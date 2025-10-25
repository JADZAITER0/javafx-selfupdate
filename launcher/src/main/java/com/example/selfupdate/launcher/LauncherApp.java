package com.example.selfupdate.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
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
    // CHANGE to your repo raw URL where manifest.json lives
    private static final String REMOTE_BASE = "https://raw.githubusercontent.com/JADZAITER0/javafx-selfupdate/tree/main/deploy/";

    private static final String MANIFEST_URL = REMOTE_BASE + "manifest.json";

    private final Path cacheDir = Paths.get(System.getProperty("user.home"), ".mylauncher", "app_cache");
    private ProgressBar progressBar;
    private Label status;

    @Override
    public void start(Stage stage) {
        Button updateBtn = new Button("Check & Update");
        Button launchBtn = new Button("Launch App");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(350);
        status = new Label("Idle");

        updateBtn.setOnAction(e -> checkAndUpdate());
        launchBtn.setOnAction(e -> {
            try {
                launchMainApp();
            } catch (IOException ex) {
                ex.printStackTrace();
                status.setText("Launch failed: " + ex.getMessage());
            }
        });

        VBox root = new VBox(10, updateBtn, launchBtn, progressBar, status);
        root.setPadding(new Insets(15));
        stage.setScene(new Scene(root));
        stage.setTitle("Launcher");
        stage.show();
    }

    private void checkAndUpdate() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Downloading manifest...");
                Files.createDirectories(cacheDir);

                String manifestJson = downloadString(MANIFEST_URL);
                if (manifestJson == null) {
                    updateMessage("Failed to download manifest");
                    return null;
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode manifest = mapper.readTree(manifestJson);
                JsonNode files = manifest.get("files");
                int total = files.size();
                int done = 0;

                Iterator<String> fieldNames = files.fieldNames();
                while (fieldNames.hasNext()) {
                    String relPath = fieldNames.next();
                    updateMessage("Checking: " + relPath);
                    String remoteHash = files.get(relPath).asText();
                    Path localFile = cacheDir.resolve(relPath);
                    boolean needsDownload = true;
                    if (Files.exists(localFile)) {
                        String localHash = sha256(localFile);
                        if (remoteHash.equals(localHash)) {
                            needsDownload = false;
                        }
                    }
                    if (needsDownload) {
                        updateMessage("Downloading " + relPath);
                        URL u = new URL(REMOTE_BASE + relPath);
                        Path parent = localFile.getParent();
                        if (parent != null) Files.createDirectories(parent);
                        try (InputStream in = u.openStream()) {
                            Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    done++;
                    updateProgress(done, total);
                }

                updateMessage("Update complete");
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());

        Thread th = new Thread(task, "updater-thread");
        th.setDaemon(true);
        th.start();
    }

    private void launchMainApp() throws IOException {
        // Launch in separate JVM so JavaFX Application.launch issues are avoided
        // Classpath = all files in cacheDir (classes root)
        // Build classpath string: cacheDir (which has package dirs) - pass as classpath root
        String javaExec = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        String mainClass = "com.example.Main";
        ProcessBuilder pb = new ProcessBuilder(javaExec, "-cp", cacheDir.toString(), mainClass);
        pb.inheritIO(); // optional: shows child stdout/stderr in launcher console
        Process p = pb.start();
        status.setText("Launched main app (pid " + p.pid() + ")");
    }

    private static String downloadString(String url) {
        try (InputStream in = new URL(url).openStream();
             BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            return sb.toString();
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