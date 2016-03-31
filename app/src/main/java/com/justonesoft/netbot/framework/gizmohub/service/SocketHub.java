package com.justonesoft.netbot.framework.gizmohub.service;

import com.justonesoft.netbot.camera.ImageStreamer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by bmunteanu on 3/31/2016.
 */
public class SocketHub implements Hub {
    private final String address;
    private final int port;
    private Socket socket;
    private ExecutorService executor;

    public SocketHub(String address, int port) {
        this.address = address;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Future<Hub> connect() {

        if (isConnected()) {
            FutureTask<Hub> dummyTask= new FutureTask<Hub>(new Runnable() {
                @Override
                public void run() {
                    //dummy doesn't do anything
                }
            },
            this);
            dummyTask.run();
            return dummyTask;
        }

        // try to establish a new connection
        return executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress( address, port ));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },
        (Hub) this);
    }

    @Override
    public Hub disconnect() {
        if (!isConnected()) return this;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
        return this;
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    @Override
    public ImageStreamer giveMeImageStreamer() {
        if (isConnected()) {
            try {
                ImageStreamer imageStreamer = new ImageStreamer(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

        }
        return null;
    }

    @Override
    public CommandListener giveMeBluetoothCommandListener(String pairedDeviceName) {
        return null;
    }
}
