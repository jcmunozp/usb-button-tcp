package com.jcmp.usbbutton.service;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyListenerService implements NativeKeyListener {
    private boolean ctrlPressed = false;
    private boolean altPressed = false;
    private static final String ACTIVACION_PATH = "C:/Users/jcmunozp/Downloads/AAA DIARIO TRABAJO/BOTON ANTIPANICO/CODIGO/COPILOT/demo-button/config/Activacion.txt";

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            ctrlPressed = true;
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            altPressed = true;
        }
        if (ctrlPressed && altPressed && e.getKeyCode() == NativeKeyEvent.VC_F12) {
            writeActivacion("Detectada combinación Ctrl+Alt+F12");
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            ctrlPressed = false;
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT) {
            altPressed = false;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // No se usa
    }

    private void writeActivacion(String mensaje) {
        try {
            Files.createDirectories(Paths.get("C:/Users/jcmunozp/Downloads/AAA DIARIO TRABAJO/BOTON ANTIPANICO/CODIGO/COPILOT/demo-button/config"));
            try (FileWriter fw = new FileWriter(ACTIVACION_PATH, true)) {
                String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                fw.write(fecha + ": " + mensaje + System.lineSeparator());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Desactivar logs de JNativeHook
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        GlobalScreen.addNativeKeyListener(new KeyListenerService());
        System.out.println("KeyListenerService iniciado. Presiona Ctrl+Alt+F12 para probar.");
    }
}
