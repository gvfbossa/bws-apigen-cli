package com.bossawebsolutions.bwsapigencli;

import com.bossawebsolutions.bwsapigencli.application.dto.AuthCache;
import com.bossawebsolutions.bwsapigencli.client.ApiClient;
import com.bossawebsolutions.bwsapigencli.model.EntityMeta;
import com.bossawebsolutions.bwsapigencli.parser.EntityParser;
import com.bossawebsolutions.bwsapigencli.scanner.EntityScanner;
import com.bossawebsolutions.bwsapigencli.utils.AuthCacheManager;
import com.bossawebsolutions.bwsapigencli.utils.FileInstaller;
import com.bossawebsolutions.bwsapigencli.utils.MachineHash;
import com.bossawebsolutions.bwsapigencli.utils.ProjectStructure;

import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    //TODO -> VIDEO!!!!!!!!!
    //TODO -> Verificar métodos de cobrança (sem plano, compra única. 1, 5 ou 10 máquinas)
    //TODO -> rebuildar a CLI pela ultima vez no Windows e LINUX e colocar no projeto do front
    //TODO -> Deploy da API na Hetzner, substtituir a URL no CLI.
    //TODO -> Deploy do front no firebase
    //TODO -> API verificar segurança - allowedOrigins

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 BWS ApiGen CLI");

        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "generate":
                generate(args);
                break;
            case "--help":
            case "-h":
                printHelp();
                break;
            case "--version":
            case "-v":
                System.out.println("BWS ApiGen CLI v1.0");
                break;
            case "logout":
                logout();
                break;
            default:
                System.out.println("❌ Unknown command: " + args[0]);
                printHelp();
        }
    }

    private static void generate(String[] args) throws Exception {
        // checa src
        File src = new File("src/main/java");
        if (!src.exists()) {
            System.out.println("❌ src/main/java not found. Run this command inside a Spring project.");
            return;
        }

        ApiClient client = new ApiClient();
        Scanner input = new Scanner(System.in);

        AuthCache cache = AuthCacheManager.load();
        String token;
        String machineHash;

        if (AuthCacheManager.isValid(cache)) {
            token = cache.getToken();
            machineHash = cache.getMachineHash();
            System.out.println("🔑 Using cached credentials for " + cache.getEmail());
        } else {
            Console console = System.console();
            String email;
            char[] passwordChars;

            if (console != null) {
                email = console.readLine("Email: ");
                passwordChars = console.readPassword("Password: ");
            } else {
                System.out.print("Email: ");
                email = input.nextLine();
                System.out.print("Password: ");
                passwordChars = input.nextLine().toCharArray();
            }

            String password = new String(passwordChars);

            machineHash = MachineHash.generate();

            try {
                token = client.login(email, password, MachineHash.generate());
            } catch (RuntimeException e) {
                System.out.println("❌ Login failed: invalid email or password.");
                return;
            }

            cache = new AuthCache();
            cache.setToken(token);
            cache.setEmail(email);
            cache.setMachineHash(machineHash);
            cache.setExpiry(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3);
            AuthCacheManager.save(cache);

            System.out.println("✅ Login successful, credentials cached");
        }

        System.out.println("🔎 Scanning entities...");
        EntityScanner scanner = new EntityScanner();
        List<File> entityFiles = scanner.scan();

        EntityParser parser = new EntityParser();
        List<EntityMeta> entities = new ArrayList<>();
        for (File file : entityFiles) entities.add(parser.parse(file));

        if (entities.isEmpty()) {
            System.out.println("⚠ No @Entity classes found. Annotate your classes with @Entity");
            return;
        }

        boolean noId = false;
        for (EntityMeta ent : entities) {
            if (ent.getIdField() == null) {
                System.out.println("❌ Entity " + ent.getName() + " does not have @Id. Please annotate your id fields.");
                noId = true;
            }
        }
        if (noId) {
            return;
        }

        System.out.println("Entities found: " + entities.size());
        for (EntityMeta e : entities) System.out.println(" - " + e.getName());

        System.out.println("🌐 Calling generator API...");
        String basePackage = ProjectStructure.detectBasePackage();

        String zipBase64 = "";
        try {
            zipBase64 = client.generate(entities, basePackage, token, machineHash);
        } catch (Exception e) {
            System.out.println("❌ Unable to contact API. please contact support at contato@bossawebsolutions.com.br - Aborting.");
            return;
        }
        if (zipBase64.isEmpty()) {
            System.out.println("❌ Unable to contact API. please contact support at contato@bossawebsolutions.com.br - Aborting.");
            return;
        }

        System.out.println("📦 Installing generated files...");
        FileInstaller.install(zipBase64);
        System.out.println("✅ Generation completed!");
    }

    private static void logout() {
        AuthCacheManager.clear();
        System.out.println("🔓 Logged out. Cache cleared!");
    }

    private static void printHelp() {
        System.out.println("""
Usage:
  bws-apigen generate        Generate REST CRUD APIs from @Entity classes
  bws-apigen logout          Logs out of your account and clears the cache
  bws-apigen --help          Show this help
  bws-apigen --version       Show CLI version

Description:
  Scans your Spring project for @Entity classes and generates
  Controllers, Services, Repositories, DTOs and Mappers.

Examples:
  bws-apigen generate
""");
    }
}