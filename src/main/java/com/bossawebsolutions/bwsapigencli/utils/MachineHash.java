package com.bossawebsolutions.bwsapigencli.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;

public class MachineHash {

    public static String generate() throws Exception {
        // pega hostname
        String hostname = InetAddress.getLocalHost().getHostName();

        // pega MAC da primeira interface de rede ativa
        String mac = "";
        for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            byte[] hardware = ni.getHardwareAddress();
            if (hardware != null && hardware.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (byte b : hardware) sb.append(String.format("%02X", b));
                mac = sb.toString();
                break;
            }
        }

        String raw = hostname + "-" + mac;

        // hash SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(raw.getBytes("UTF-8"));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}