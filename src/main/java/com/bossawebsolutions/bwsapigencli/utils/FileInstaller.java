package com.bossawebsolutions.bwsapigencli.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Stream;

public class FileInstaller {

    public static void install(String zipBase64) throws Exception {

        // 1️⃣ decodifica o zip
        byte[] zipBytes = Base64.getDecoder().decode(zipBase64);

        File zipFile = new File("generated.zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            fos.write(zipBytes);
        }

        // 2️⃣ detecta o basePackage do projeto
        String basePackage = detectBasePackage();
        String packagePath = basePackage.replace(".", "/");
        File targetDir = new File("src/main/java/" + packagePath);

        // 3️⃣ descompacta no caminho correto
        unzip(zipFile, targetDir);

        // 4️⃣ apaga o zip
        zipFile.delete();

        System.out.println("✔ Files installed in " + targetDir.getPath());
    }

    private static String detectBasePackage() throws IOException {

        File src = new File("src/main/java");
        if (!src.exists()) {
            throw new RuntimeException("src/main/java not found");
        }

        try (Stream<File> files = Files.walk(src.toPath())
                .map(p -> p.toFile())
                .filter(f -> f.getName().endsWith(".java"))) {

            Optional<File> appFile = files
                    .filter(FileInstaller::containsSpringBootApplication)
                    .findFirst();

            if (appFile.isPresent()) {
                return extractPackage(appFile.get());
            }
        }

        throw new RuntimeException("Could not detect @SpringBootApplication class");
    }

    private static boolean containsSpringBootApplication(File file) {
        try {
            return Files.readString(file.toPath())
                    .contains("@SpringBootApplication");
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractPackage(File file) throws IOException {
        for (String line : Files.readAllLines(file.toPath())) {
            line = line.trim();
            if (line.startsWith("package ")) {
                return line.replace("package", "").replace(";", "").trim();
            }
        }
        throw new RuntimeException("Package declaration not found in " + file.getName());
    }

    private static void unzip(File zipFile, File targetDir) throws Exception {

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                File newFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                }

                newFile.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    zis.transferTo(fos);
                }

                System.out.println("✔ Installed: " + newFile.getPath());
            }
        }
    }
}