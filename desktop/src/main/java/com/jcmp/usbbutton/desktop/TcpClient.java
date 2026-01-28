package com.jcmp.usbbutton.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Map;

public class TcpClient {
    private final String host; private final int port; private final String token; private final TrayNotifier notifier;
    private volatile boolean running = true;

    public TcpClient(String host, int port, String token, TrayNotifier notifier) {
        this.host=host; this.port=port; this.token=token; this.notifier=notifier;
    }
    public void start(){ new Thread(this::loop, "tcp-client").start(); }
    public void stop(){ running=false; }

    private void loop() {
        var mapper = new ObjectMapper(); long backoff = 1000;
        while (running) {
            try (Socket s = new Socket(host, port)) {
                s.setTcpNoDelay(true);
                var out = s.getOutputStream(); var in = s.getInputStream();
                writeJson(out, mapper, Map.of("type","AUTH","token",token,"client","DeskApp"));
                Map<String,Object> reply = readJson(in, mapper);
                if (reply==null || !"AUTH_OK".equals(reply.get("type"))) { notifier.error("Conexión","Auth fallida"); Thread.sleep(5000); continue; }
                notifier.info("Conectado","Cliente conectado al servicio"); backoff=1000;

                Map<String,Object> msg;
                while (running && (msg = readJson(in, mapper)) != null) {
                    String type = String.valueOf(msg.get("type"));
                    switch (type) {
                        case "BUTTON_PRESS" -> {
                            String code = String.valueOf(msg.get("code"));
                            notifier.info("Botón pulsado", "Código: " + code);
                        }
                        case "STATUS" -> notifier.info("Estado", String.valueOf(msg.get("message")));
                    }
                }
            } catch (Exception e) {
                try { Thread.sleep(backoff); backoff = Math.min(backoff*2, Duration.ofMinutes(1).toMillis()); } catch (InterruptedException ignored) {}
            }
        }
    }

    private static void writeJson(OutputStream out, ObjectMapper mapper, Map<String,Object> msg) throws IOException {
        byte[] data = mapper.writeValueAsBytes(msg);
        ByteBuffer len = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(data.length);
        out.write(len.array()); out.write(data); out.flush();
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> readJson(InputStream in, ObjectMapper mapper) throws IOException {
        byte[] hdr = in.readNBytes(4); if (hdr.length < 4) return null;
        int len = ByteBuffer.wrap(hdr).order(ByteOrder.BIG_ENDIAN).getInt();
        if (len <= 0 || len > 10_000_000) throw new IOException("Frame length invalid: " + len);
        byte[] payload = in.readNBytes(len); if (payload.length < len) return null;
        return mapper.readValue(payload, Map.class);
    }
}
