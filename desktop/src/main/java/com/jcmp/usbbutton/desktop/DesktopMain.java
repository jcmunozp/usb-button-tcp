package com.jcmp.usbbutton.desktop;

public class DesktopMain {
    public static void main(String[] args) throws Exception {
        Config cfg = Config.load();
        TrayNotifier tray = new TrayNotifier(); tray.init();
        TcpClient client = new TcpClient(cfg.host, cfg.port, cfg.token, tray);
        client.start();
        Thread.currentThread().join();
    }
}
