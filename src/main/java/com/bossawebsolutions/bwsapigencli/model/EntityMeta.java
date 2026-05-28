package com.bossawebsolutions.bwsapigencli.model;

import lombok.Data;

import java.util.List;

@Data
public class EntityMeta {

    private String name;

    private String idField;

    private String idType;

    private List<FieldMeta> fields;

    private String packageName;

    private String source;

    public boolean hasGetters() {
        if (source == null) {
            return false;
        }
        if (source.contains("@Data") ||
                source.contains("@Getter") ||
                source.contains("@Setter")) {
            return true;
        }
        for (FieldMeta field : fields) {
            String getter = "get" + capitalize(field.getName()) + "(";
            if (!source.contains(getter)) {
                return false;
            }
        }
        return true;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}