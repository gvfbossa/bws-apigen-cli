package com.bossawebsolutions.bwsapigencli.parser;

import com.bossawebsolutions.bwsapigencli.model.EntityMeta;
import com.bossawebsolutions.bwsapigencli.model.FieldMeta;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class EntityParser {

    public EntityMeta parse(File file) throws Exception {

        ParserConfiguration config = new ParserConfiguration();
        config.setAttributeComments(false);
        config.setLexicalPreservationEnabled(false);
        config.setStoreTokens(false);
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.RAW);

        JavaParser parser = new JavaParser(config);
        ParseResult<CompilationUnit> result = parser.parse(file);

        if (result.getResult().isEmpty()) {
            throw new RuntimeException("Failed to parse file");
        }

        CompilationUnit cu = result.getResult().get();

        String source = Files.readString(file.toPath());

        ClassOrInterfaceDeclaration clazz =
                cu.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();

        EntityMeta entity = new EntityMeta();
        entity.setName(clazz.getNameAsString());
        entity.setSource(source);

        String packageName = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse("");
        entity.setPackageName(packageName);

        List<FieldMeta> fields = new ArrayList<>();

        for (FieldDeclaration field : clazz.getFields()) {

            String type = field.getElementType().asString();
            String name = field.getVariable(0).getNameAsString();

            FieldMeta meta = new FieldMeta();
            meta.setName(name);
            meta.setType(type);

            fields.add(meta);

            if (field.isAnnotationPresent("Id")) {
                entity.setIdField(name);
                entity.setIdType(type);
            }
        }

        entity.setFields(fields);

        return entity;
    }
}