package com.jcmp.usbbutton.service;

import java.util.concurrent.atomic.AtomicBoolean;
import com.github.kwhat.jnativehook.GlobalScreen;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainService {
    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        Config cfg = Config.load();
        TcpServer server = new TcpServer(cfg.port, cfg.token);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running.set(false); server.stop();
        }));

        // Iniciar escucha global de teclas (Ctrl+A)
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new KeyListenerService());
        } catch (Exception e) {
            System.err.println("No se pudo iniciar el listener global de teclado: " + e.getMessage());
        }

        // Simulación: emite un evento cada 10 segundos
        Thread sim = new Thread(() -> {
            int i=0; while (running.get()) {
                try { Thread.sleep(10_000); server.emitButtonPress("BTN"+(++i)); } catch (InterruptedException ignored) {}
            }
        }, "sim"); sim.setDaemon(true); sim.start();

        while (running.get()) Thread.sleep(1000);
    }
}
