package com.samjakob.websocket_minecraft;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ThornhillAPI extends WebSocketServer {

    private final List<WebSocket> connectedSockets;

    public ThornhillAPI(InetSocketAddress address) {
        super(address);
        connectedSockets = new ArrayList<>();
    }

    @Override
    public void onOpen(WebSocket connection, ClientHandshake handshake) {

    }

    @Override
    public void onMessage(WebSocket connection, String message) {
        if (message.equals(Main.getInstance().getConfig().get("websocket-server.api-key")) && !connectedSockets.contains(connection)) {
            connectedSockets.add(connection);
            connection.send("{\"authenticated\": true}");
        }
    }

    @Override
    public void onError(WebSocket connection, Exception exception) {

    }

    @Override
    public void onClose(WebSocket connection, int code, String reason, boolean remote) {
        connectedSockets.remove(connection);
    }

    public void broadcastThornhillMessage(String message) {
        connectedSockets.forEach(socket -> socket.send(message));
    }

    @Override
    public void onStart() {
        Main.getInstance().getLogger().log(Level.INFO, "WebSocket server started successfully.");
    }

}
