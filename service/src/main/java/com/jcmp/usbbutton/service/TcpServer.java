package com.jcmp.usbbutton.service;

import java.io.IOException;
import java.net.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

public class TcpServer {
    private final int port;
    private final String token;
    private final EventBus bus = new EventBus();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private ServerSocket server;

    public TcpServer(int port, String token) { this.port=port; this.token=token; }

    public void start() throws IOException {
        server = new ServerSocket();
        server.bind(new InetSocketAddress("127.0.0.1", port), 50);
        pool.submit(() -> {
            while (running) {
                try {
                    Socket s = server.accept(); s.setTcpNoDelay(true);
                    pool.submit(new ClientSession(s, token, bus));
                } catch (IOException e) { if (running) e.printStackTrace(); }
            }
        });
    }
    public void stop() { running=false; try { server.close(); } catch(Exception ignored){} pool.shutdownNow(); }

    public void emitButtonPress(String code){
        bus.broadcast(Map.of("type","BUTTON_PRESS", "code",code, "ts", Instant.now().toString(), "meta", Map.of("source","HID")));
    }
    public void emitStatus(String level, String message){
        bus.broadcast(Map.of("type","STATUS","level",level,"message",message,"ts",Instant.now().toString()));
    }
}
