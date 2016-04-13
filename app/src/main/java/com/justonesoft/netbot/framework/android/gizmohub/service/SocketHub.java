package com.justonesoft.netbot.framework.android.gizmohub.service;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import com.justonesoft.netbot.bt.BTController;
import com.justonesoft.netbot.camera.ImageStreamer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
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
    private Future<Socket> futureForSocket;
    public SocketHub(String address, int port) {
        this.address = address;
        this.port = port;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Hub connect() {
        if (isConnected()) {
            return this;
        }

        // try to establish a new connection
        futureForSocket = executor.submit(new Callable<Socket>() {
            @Override
            public Socket call() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port));
                    return socket;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        return this;
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
        Future<Socket> futureSocket = SocketManager.newConnectedSocket();
        return new BTCommandListener(pairedDeviceName);
    }

    @Override
    public CommandListener giveMeUICommandListener(Handler uiHandler) {
        Future<Socket> futureSocket = SocketManager.newConnectedSocket();
        return new UICommandListener(uiHandler);
    }
}
