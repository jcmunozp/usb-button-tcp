package com.jcmp.usbbutton.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientSession implements Runnable {
    private final Socket socket;
    private final MessageCodec codec = new MessageCodec();
    private final String expectedToken;
    private final EventBus bus;
    private final AtomicBoolean authed = new AtomicBoolean(false);

    public ClientSession(Socket s, String expectedToken, EventBus bus) {
        this.socket = s; this.expectedToken = expectedToken; this.bus = bus;
    }

    @Override public void run() {
        try (socket) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            var first = codec.readJson(in);
            if (first == null || !"AUTH".equals(first.get("type"))) {
                codec.writeJson(out, Map.of("type","AUTH_FAIL","reason","missing AUTH")); return;
            }
            String token = String.valueOf(first.get("token"));
            if (!expectedToken.equals(token)) { codec.writeJson(out, Map.of("type","AUTH_FAIL","reason","invalid token")); return; }
            authed.set(true);
            codec.writeJson(out, Map.of("type","AUTH_OK"));
            bus.registerClient(this);

            Map<String,Object> msg;
            while ((msg = codec.readJson(in)) != null) {
                String type = String.valueOf(msg.get("type"));
                if ("PING".equals(type)) codec.writeJson(out, Map.of("type","PONG","ts", Instant.now().toString()));
            }
        } catch (Exception ignored) {} finally { bus.unregisterClient(this); }
    }

    public synchronized void send(Map<String,Object> msg) {
        try { codec.writeJson(socket.getOutputStream(), msg); } catch (Exception e) { try { socket.close(); } catch (Exception ignored) {} }
    }
}
