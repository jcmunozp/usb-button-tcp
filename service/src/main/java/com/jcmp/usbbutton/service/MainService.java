package com.jcmp.usbbutton.service;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainService {
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        Config cfg = Config.load();
        TcpServer server = new TcpServer(cfg.port, cfg.token);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false); server.stop();
        }));

        // Simulación: emite un evento cada 10 segundos
        Thread sim = new Thread(() -> {
            int i=0; while (running.get()) {
                try { Thread.sleep(10_000); server.emitButtonPress("BTN"+(++i)); } catch (InterruptedException ignored) {}
            }
        }, "sim"); sim.setDaemon(true); sim.start();

        while (running.get()) Thread.sleep(1000);
    }
}
