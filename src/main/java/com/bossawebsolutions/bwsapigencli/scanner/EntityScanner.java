package com.bossawebsolutions.bwsapigencli.scanner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntityScanner {

    private final JavaParser parser;

    public EntityScanner() {
        ParserConfiguration config = new ParserConfiguration();
        config.setAttributeComments(false);
        config.setLexicalPreservationEnabled(false);
        config.setStoreTokens(false);
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.RAW);

        this.parser = new JavaParser(config);
    }

    public List<File> scan() throws Exception {

        List<File> entities = new ArrayList<>();

        File root = new File("src/main/java");

        scanFolder(root, entities);

        return entities;
    }

    private void scanFolder(File folder, List<File> entities) throws Exception {

        if (!folder.exists()) return;

        for (File file : Objects.requireNonNull(folder.listFiles())) {

            if (file.isDirectory()) {
                scanFolder(file, entities);
                continue;
            }

            if (!file.getName().endsWith(".java")) {
                continue;
            }

            ParseResult<CompilationUnit> result = parser.parse(file);

            if (result.getResult().isEmpty()) {
                continue; // ignora arquivos que não parsearam
            }

            CompilationUnit cu = result.getResult().get();

            boolean isEntity = cu.findAll(ClassOrInterfaceDeclaration.class)
                    .stream()
                    .anyMatch(clazz ->
                            clazz.getAnnotations()
                                    .stream()
                                    .anyMatch(a ->
                                            a.getNameAsString().equals("Entity")
                                    )
                    );

            if (isEntity) {
                entities.add(file);
            }
        }
    }
}