package com.justonesoft.netbot.framework.android.gizmohub.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bmunteanu on 4/7/2016.
 */
public class SocketManager {
    private static String serverAddress = "";
    private static int serverPort = 0;
    private static ExecutorService executor;

    public static void init(String serverAddress, int port) {
        SocketManager.serverAddress = serverAddress;
        SocketManager.serverPort = port;
        executor = Executors.newSingleThreadExecutor();
    }

    public static Future<Socket> newConnectedSocket() {
        // try to establish a new connection
        return executor.submit(new Callable<Socket>() {
            @Override
            public Socket call() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(SocketManager.serverAddress, SocketManager.serverPort));
                    return socket;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }
}
