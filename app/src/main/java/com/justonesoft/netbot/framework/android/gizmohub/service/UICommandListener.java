package com.justonesoft.netbot.framework.android.gizmohub.service;

import android.os.Handler;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * Created by bmunteanu on 4/1/2016.
 *
 * Waits for something to come over InputStream and updates the UI accordingly
 *
 */
public class UICommandListener implements CommandListener {
    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private DataInputStream dataStream;

    private Handler uiHandler;
    private Future<Socket> futureForSocket;

    public UICommandListener(Future<Socket> futureForSocket, Handler uiHandler) {
        this.futureForSocket = futureForSocket;
        this.uiHandler = uiHandler;
    }

    public UICommandListener(InputStream inputStream, Handler uiHandler) {
        dataStream = new DataInputStream(inputStream);
        this.uiHandler = uiHandler;
    }

    @Override
    public void listenForCommand() {

    }

    @Override
    public void listenAndExecuteCommands() {
        threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = futureForSocket.get();
                    if (socket == null) return;

                    dataStream = new DataInputStream(socket.getInputStream());
                    uiHandler.obtainMessage(1, "Waiting for data").sendToTarget();
                    while (true) {
                        try {
                            int dataFromSocket = dataStream.read(); // or use readByte and catch EOFException
                            if (dataFromSocket == -1) {
                                uiHandler.obtainMessage(1, "End of transmission").sendToTarget();
                                break;
                            }
                            uiHandler.obtainMessage(1, "Command received: " + dataFromSocket).sendToTarget();
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
