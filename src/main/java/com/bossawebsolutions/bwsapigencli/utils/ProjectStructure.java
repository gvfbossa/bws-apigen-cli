package com.bossawebsolutions.bwsapigencli.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Stream;

public class ProjectStructure {

    // Detecta o package base do projeto automaticamente
    public static String detectBasePackage() {

        File src = new File("src/main/java");
        if (!src.exists()) {
            throw new RuntimeException("src/main/java not found");
        }

        try (Stream<File> files = Files.walk(src.toPath())
                .map(p -> p.toFile())
                .filter(f -> f.getName().endsWith(".java"))) {

            Optional<File> appFile = files
                    .filter(ProjectStructure::containsSpringBootApplication)
                    .findFirst();

            if (appFile.isPresent()) {
                return extractPackage(appFile.get());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Could not detect @SpringBootApplication class");
    }

    // Retorna o caminho físico base de acordo com o package detectado
    public static File getBaseJavaPath() {
        String basePackage = detectBasePackage();
        String packagePath = basePackage.replace(".", "/");
        return new File("src/main/java/" + packagePath);
    }

    private static boolean containsSpringBootApplication(File file) {
        try {
            return Files.readString(file.toPath())
                    .contains("@SpringBootApplication");
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractPackage(File file) {
        try {
            for (String line : Files.readAllLines(file.toPath())) {
                line = line.trim();
                if (line.startsWith("package ")) {
                    return line
                            .replace("package", "")
                            .replace(";", "")
                            .trim();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Package declaration not found in " + file.getName());
    }
}