package com.example.selfupdate.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class LauncherApp extends Application {
    private static final String REMOTE_BASE = "https://raw.githubusercontent.com/JADZAITER0/javafx-selfupdate/main/deploy/";
    private static final String MANIFEST_URL = REMOTE_BASE + "manifest.json";

    private final Path cacheDir = Paths.get(System.getProperty("user.home"), ".mylauncher", "app_cache");
    private ProgressBar progressBar;
    private Label status;
    private TextArea logArea;

    @Override
    public void start(Stage stage) {
        Button updateBtn = new Button("Check & Update");
        Button launchBtn = new Button("Launch App");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(350);
        status = new Label("Idle");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(10);
        logArea.setWrapText(true);

        updateBtn.setOnAction(e -> checkAndUpdate());
        launchBtn.setOnAction(e -> {
            try {
                launchMainApp();
            } catch (IOException ex) {
                ex.printStackTrace();
                log("Launch failed: " + ex.getMessage());
                status.textProperty().unbind();
                status.setText("Launch failed: " + ex.getMessage());
            }
        });

        VBox root = new VBox(10, updateBtn, launchBtn, progressBar, status, new Label("Log:"), logArea);
        root.setPadding(new Insets(15));
        stage.setScene(new Scene(root, 500, 500));
        stage.setTitle("Launcher");
        stage.show();

        log("Launcher started");
        log("Remote base: " + REMOTE_BASE);
        log("Cache dir: " + cacheDir);
    }

    private void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            System.out.println("[LAUNCHER] " + message);
        });
    }

    private void checkAndUpdate() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    updateMessage("Downloading manifest...");
                    log("Starting update check...");
                    Files.createDirectories(cacheDir);
                    log("Cache directory created/verified: " + cacheDir);

                    log("Downloading manifest from: " + MANIFEST_URL);
                    String manifestJson = downloadString(MANIFEST_URL);
                    if (manifestJson == null || manifestJson.trim().isEmpty()) {
                        updateMessage("Failed to download manifest");
                        log("ERROR: Manifest download failed or empty");
                        return null;
                    }
                    log("Manifest downloaded successfully (" + manifestJson.length() + " bytes)");

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode manifest = mapper.readTree(manifestJson);
                    log("Manifest parsed successfully");

                    JsonNode files = manifest.get("files");
                    if (files == null) {
                        log("ERROR: No 'files' node in manifest");
                        updateMessage("Invalid manifest format");
                        return null;
                    }

                    int total = files.size();
                    log("Files to check: " + total);
                    int done = 0;
                    int failedDownloads = 0;

                    Iterator<String> fieldNames = files.fieldNames();
                    while (fieldNames.hasNext()) {
                        String relPath = fieldNames.next();
                        updateMessage("Checking: " + relPath);
                        log("Checking file: " + relPath);

                        String remoteHash = files.get(relPath).asText();

                        Path localFile = cacheDir.resolve(relPath);
                        boolean needsDownload = true;

                        if (Files.exists(localFile)) {
                            String localHash = sha256(localFile);
                            if (remoteHash.equals(localHash)) {
                                needsDownload = false;
                                log("  File up-to-date: " + relPath);
                            } else {
                                log("  File outdated: " + relPath);
                            }
                        } else {
                            log("  File missing: " + relPath);
                        }

                        if (needsDownload) {
                            updateMessage("Downloading " + relPath);
                            String fileUrl = REMOTE_BASE + relPath;
                            log("  Downloading from: " + fileUrl);

                            try {
                                URL u = new URL(fileUrl);
                                Path parent = localFile.getParent();
                                if (parent != null) Files.createDirectories(parent);

                                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                                conn.setConnectTimeout(10000);
                                conn.setReadTimeout(30000);

                                int responseCode = conn.getResponseCode();

                                if (responseCode == 200) {
                                    try (InputStream in = conn.getInputStream()) {
                                        Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
                                        log("  Downloaded: " + relPath);
                                    }
                                } else {
                                    log("  ERROR: HTTP " + responseCode + " for " + relPath);
                                    failedDownloads++;
                                }
                            } catch (Exception ex) {
                                log("  ERROR downloading: " + ex.getMessage());
                                failedDownloads++;
                            }
                        }

                        done++;
                        updateProgress(done, total);
                    }

                    if (failedDownloads > 0) {
                        updateMessage("Update completed with " + failedDownloads + " failed downloads");
                        log("WARNING: " + failedDownloads + " files failed to download");
                    } else {
                        updateMessage("Update complete");
                        log("Update completed successfully!");
                    }
                    return null;

                } catch (Exception ex) {
                    log("ERROR: " + ex.getClass().getName() + ": " + ex.getMessage());
                    ex.printStackTrace();
                    throw ex;
                }
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());

        Thread th = new Thread(task, "updater-thread");
        th.setDaemon(true);
        th.start();
    }

    private void launchMainApp() throws IOException {
        status.textProperty().unbind();
        log("Launching main application...");

        String javaExec = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        String mainClass = "com.dynamic.mainapp.Launcher";

        // Build module path for child process
        String modulePath = buildModulePathForChild();

        List<String> command = new ArrayList<>();
        command.add(javaExec);
        command.add("--module-path");
        command.add(modulePath);
        command.add("--add-modules");
        command.add("javafx.controls,javafx.fxml,javafx.graphics,javafx.base");
        command.add("-cp");
        command.add(cacheDir.toString());
        command.add(mainClass);

        log("Launch command: " + String.join(" ", command));
        log("Module path: " + modulePath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();

        try {
            Process p = pb.start();
            status.setText("Launched main app (pid " + p.pid() + ")");
            log("Main app launched with PID: " + p.pid());
        } catch (IOException ex) {
            log("ERROR launching main app: " + ex.getMessage());
            status.setText("Launch failed: " + ex.getMessage());
        }
    }

    private String buildModulePathForChild() {
        List<String> modulePathEntries = new ArrayList<>();
        modulePathEntries.add(cacheDir.toString());

        String javaHome = System.getProperty("java.home");

        // Check if we're running in a jpackage environment
        if (isJpackageEnvironment()) {
            log("Detected jpackage environment");
            // In jpackage, modules are in the runtime's jmods directory
            // But we need to use the lib directory for child processes
            Path libDir = Paths.get(javaHome, "lib");
            if (Files.exists(libDir)) {
                modulePathEntries.add(libDir.toString());
                log("Added lib directory to module path: " + libDir);
            }
            modulePathEntries.add(javaHome);
        } else {
            // Development environment - use standard module paths
            String modulePath = System.getProperty("jdk.module.path");
            String appModulePath = System.getProperty("java.app.module.path");

            if (modulePath != null && !modulePath.isEmpty()) {
                Collections.addAll(modulePathEntries, modulePath.split(File.pathSeparator));
            }
            if (appModulePath != null && !appModulePath.isEmpty()) {
                Collections.addAll(modulePathEntries, appModulePath.split(File.pathSeparator));
            }

            // Fallback: add Java home
            modulePathEntries.add(javaHome);
        }

        return String.join(File.pathSeparator, modulePathEntries);
    }

    private boolean isJpackageEnvironment() {
        String javaHome = System.getProperty("java.home");
        // jpackage runtimes often have specific directory structures
        return javaHome.contains("runtime") ||
                javaHome.contains(".app") || // Mac
                Files.exists(Paths.get(javaHome, "lib", "jpackage.jar")) ||
                System.getProperty("jpackage.app-path") != null;
    }

    private List<String> findJavaFXJars() {
        List<String> javafxJars = new ArrayList<>();

        try {
            // Look for ALL JavaFX modules
            String[] javafxModuleClasses = {
                    "javafx.application.Application",      // javafx.graphics
                    "javafx.beans.property.Property",      // javafx.base
                    "javafx.scene.control.Button",         // javafx.controls
                    "javafx.fxml.FXML",                    // javafx.fxml
                    "javafx.animation.Animation"           // javafx.graphics
            };

            for (String className : javafxModuleClasses) {
                try {
                    Class<?> clazz = Class.forName(className);
                    String jarPath = getJarPathFromClass(clazz);
                    if (jarPath != null && !javafxJars.contains(jarPath)) {
                        javafxJars.add(jarPath);
                        log("Found JavaFX JAR: " + jarPath + " (from " + className + ")");
                    }
                } catch (ClassNotFoundException e) {
                    log("WARNING: JavaFX class not found: " + className);
                }
            }

        } catch (Exception e) {
            log("ERROR: Failed to find JavaFX JARs: " + e.getMessage());
        }

        return javafxJars;
    }

    private String getJarPathFromClass(Class<?> clazz) {
        try {
            String classFile = clazz.getName().replace('.', '/') + ".class";
            URL resource = clazz.getClassLoader().getResource(classFile);
            if (resource != null) {
                String urlString = resource.toString();
                if (urlString.startsWith("jar:file:")) {
                    // Extract JAR file path from URL: jar:file:/path/to/jar.jar!/package/Class.class
                    int exclamation = urlString.indexOf('!');
                    if (exclamation != -1) {
                        String jarPath = urlString.substring(9, exclamation); // Remove "jar:file:"
                        return new File(jarPath).getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            log("Error getting JAR path for class: " + clazz.getName());
        }
        return null;
    }

    private void addJacksonToClasspath(List<String> classpath, String m2Repo, String version) {
        String[] jacksonModules = {
                "jackson-databind",
                "jackson-core",
                "jackson-annotations"
        };

        for (String module : jacksonModules) {
            String jarPath = Paths.get(m2Repo, "com", "fasterxml", "jackson", "core",
                    module, version, module + "-" + version + ".jar").toString();

            if (Files.exists(Paths.get(jarPath))) {
                classpath.add(jarPath);
                log("Classpath: " + jarPath);
            } else {
                log("WARNING: Jackson JAR not found: " + jarPath);
            }
        }
    }

    private static String downloadString(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            try (InputStream in = conn.getInputStream();
                 BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line).append("\n");
                return sb.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String sha256(Path path) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] data = Files.readAllBytes(path);
            byte[] d = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}