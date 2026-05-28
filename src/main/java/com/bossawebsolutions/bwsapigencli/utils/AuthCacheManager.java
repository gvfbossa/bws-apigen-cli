package com.bossawebsolutions.bwsapigencli.utils;

import com.bossawebsolutions.bwsapigencli.application.dto.AuthCache;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthCacheManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CONFIG_PATH = System.getProperty("user.home") + "/.bwsapigen/config.json";

    public static AuthCache load() {
        try {
            File f = new File(CONFIG_PATH);
            if (!f.exists()) return null;
            return mapper.readValue(f, AuthCache.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static void save(AuthCache cache) {
        try {
            File dir = new File(System.getProperty("user.home") + "/.bwsapigen");
            if (!dir.exists()) dir.mkdirs();
            mapper.writeValue(Paths.get(CONFIG_PATH).toFile(), cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isValid(AuthCache cache) {
        return cache != null && cache.getToken() != null && System.currentTimeMillis() < cache.getExpiry();
    }

    public static void clear() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("🔓 Auth cache cleared!");
            } else {
                System.out.println("⚠ Could not delete auth cache file.");
            }
        } else {
            System.out.println("⚠ No auth cache found to clear.");
        }
    }
}