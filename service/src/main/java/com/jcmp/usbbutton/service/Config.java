package com.jcmp.usbbutton.service;

import java.io.FileInputStream;
import java.nio.file.*;
import java.util.Properties;

public final class Config {
    public final int port;
    public final String token;

    private Config(int port, String token) {
        this.port = port; this.token = token;
    }

    public static Config load() throws Exception {
        Path sysCfg = Paths.get("C:\ProgramData\UsbButton\config.properties");
        Properties p = new Properties();
        if (Files.exists(sysCfg)) try (var in = new FileInputStream(sysCfg.toFile())) { p.load(in); }

        String portStr = System.getProperty("svc.port", p.getProperty("port", "50515"));
        String token   = System.getProperty("svc.token", p.getProperty("token", "CHANGE_ME"));

        int port = Integer.parseInt(portStr);
        if (token == null || token.isBlank() || "CHANGE_ME".equals(token)) {
            throw new IllegalStateException("Token no válido. Configura C:\ProgramData\UsbButton\config.properties");
        }
        return new Config(port, token);
    }
}
