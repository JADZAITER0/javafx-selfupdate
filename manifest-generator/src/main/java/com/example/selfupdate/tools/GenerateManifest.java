package com.example.selfupdate.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class GenerateManifest {
    public static void main(String[] args) throws Exception {
        // args[0] = path to classes dir (e.g., ../main-app/target/classes)
        // args[1] = output file (e.g., ../deploy/manifest.json)
        if (args.length < 2) {
            System.err.println("Usage: GenerateManifest <classesDir> <outManifestJson>");
            System.exit(2);
        }

        Path classesDir = Paths.get(args[0]);
        Path outManifest = Paths.get(args[1]);
        if (!Files.isDirectory(classesDir)) {
            System.err.println("classesDir not found: " + classesDir);
            System.exit(3);
        }

        Map<String, String> files = new TreeMap<>();
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Walk the classes directory and compute SHA-256 for each file
        Files.walk(classesDir)
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        byte[] data = Files.readAllBytes(p);
                        byte[] digest = md.digest(data);
                        md.reset();
                        String hex = bytesToHex(digest);
                        String rel = classesDir.relativize(p).toString().replace('\\', '/');
                        files.put(rel, hex);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });

        // Create ObjectMapper and JSON structure
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print

        ObjectNode manifest = mapper.createObjectNode();
        manifest.put("version", String.valueOf(System.currentTimeMillis())); // simple timestamp version
        manifest.set("files", mapper.valueToTree(files));

        // Ensure parent directories exist
        Files.createDirectories(outManifest.getParent());

        // Write JSON to file
        mapper.writeValue(outManifest.toFile(), manifest);

        System.out.println("Manifest generated: " + outManifest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}