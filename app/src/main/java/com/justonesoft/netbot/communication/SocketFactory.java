package com.justonesoft.netbot.communication;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bmunteanu on 3/9/2016.
 */
public class SocketFactory {

    // must have a server address, hostName and port, to connect to
    // creates a new CONNECTED socket

    public static Socket getConnectedSocket(String host, int port) throws IOException {
        return new Socket(host, port);
    }

    public static  Socket getConnectedSocket() {
        try {
            return new Socket("52.36.146.169", 9999);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
