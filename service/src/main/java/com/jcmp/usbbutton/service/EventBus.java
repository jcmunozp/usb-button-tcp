package com.jcmp.usbbutton.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventBus {
    private final Set<ClientSession> clients = new CopyOnWriteArraySet<>();
    void registerClient(ClientSession c){ clients.add(c); }
    void unregisterClient(ClientSession c){ clients.remove(c); }
    void broadcast(Map<String,Object> msg){ clients.forEach(c -> c.send(msg)); }
}
