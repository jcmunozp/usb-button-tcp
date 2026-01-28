package com.jcmp.usbbutton.desktop;

import java.io.FileInputStream;
import java.nio.file.*;
import java.util.Properties;

public final class Config {
    public final String host;
    public final int port;
    public final String token;

    private Config(String host, int port, String token) {
        this.host=host; this.port=port; this.token=token;
    }

    public static Config load() throws Exception {
        Path sysCfg = Paths.get("C:\ProgramData\UsbButton\config.properties");
        Properties p = new Properties();
        if (Files.exists(sysCfg)) try (var in = new FileInputStream(sysCfg.toFile())) { p.load(in); }

        String host = System.getProperty("desk.host", "127.0.0.1");
        String portStr = System.getProperty("desk.port", p.getProperty("port", "50515"));
        String token   = System.getProperty("desk.token", p.getProperty("token", "CHANGE_ME"));

        int port = Integer.parseInt(portStr);
        if (token == null || token.isBlank() || "CHANGE_ME".equals(token))
            throw new IllegalStateException("Token no válido. Configura C:\ProgramData\UsbButton\config.properties");
        return new Config(host, port, token);
    }
}
