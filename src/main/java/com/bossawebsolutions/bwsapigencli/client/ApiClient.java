package com.bossawebsolutions.bwsapigencli.client;

import com.bossawebsolutions.bwsapigencli.application.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bossawebsolutions.bwsapigencli.model.EntityMeta;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static boolean dev = false;

    private static final String BASE_URL = dev ? "https://api.bwsapigen.bossawebsolutions.com.br" : "http://localhost:8080";

    // ------------------------
    // LOGIN
    // ------------------------
    public String login(String email, String password, String machineHash) throws Exception {
        URL url = new URL(BASE_URL + "/auth/login");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        // Serializa o corpo
        String json = mapper.writeValueAsString(
                Map.of(
                        "email", email,
                        "password", password,
                        "machineHash", machineHash
                )
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int code = conn.getResponseCode();
        InputStream is = code == 200 ? conn.getInputStream() : conn.getErrorStream();
        String resp = new String(is.readAllBytes());

        if (code != 200) {
            throw new RuntimeException("Login failed: " + code + " -> " + resp);
        }
        LoginResponse loginResp = mapper.readValue(resp, LoginResponse.class);

        return loginResp.getToken();
    }

    // ------------------------
    // GENERATE
    // ------------------------
    public String generate(List<EntityMeta> entities, String basePackage, String token, String machineHash) throws Exception {
        URL url = new URL(BASE_URL + "/api/generate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("X-Machine-Hash", machineHash);

        String json = mapper.writeValueAsString(
                Map.of(
                        "entities", entities,
                        "basePackage", basePackage
                )
        );

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int code = conn.getResponseCode();

        InputStream is;

        if (code >= 200 && code < 300) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();

            if (is == null) {
                throw new RuntimeException("API error: " + code + " (no response body)");
            }
        }

        String resp = new String(is.readAllBytes());

        if (code != 200) {
            throw new RuntimeException("API error: " + code + " -> " + resp);
        }

        return resp;
    }
}