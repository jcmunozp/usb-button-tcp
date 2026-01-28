package com.jcmp.usbbutton.desktop;

import java.awt.*;

public class TrayNotifier {
    private TrayIcon tray;
    public void init() throws Exception {
        if (!SystemTray.isSupported()) throw new UnsupportedOperationException("SystemTray not supported");
        var sys = SystemTray.getSystemTray();
        Image image = new java.awt.image.BufferedImage(16,16,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        tray = new TrayIcon(image, "USB Button Client"); tray.setImageAutoSize(true);
        sys.add(tray);
    }
    public void info(String caption, String text){ if (tray!=null) tray.displayMessage(caption, text, TrayIcon.MessageType.INFO); }
    public void error(String caption, String text){ if (tray!=null) tray.displayMessage(caption, text, TrayIcon.MessageType.ERROR); }
}
