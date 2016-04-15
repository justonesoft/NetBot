package com.justonesoft.netbot.framework.android.gizmohub.service;

import android.os.Handler;

import com.justonesoft.netbot.framework.android.gizmohub.protocol.Message;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.MessageType;

/**
 * Created by bmunteanu on 4/1/2016.
 *
 * Waits for something to come over InputStream and updates the UI accordingly
 *
 */
public class UICommandListener implements CommandListener<Integer> {

    private Handler uiHandler;

    public UICommandListener(Handler uiHandler) {
        this.uiHandler = uiHandler;
    }

    @Override
    public boolean isInterestedIn(MessageType messageType) {
        return MessageType.UI.equals(messageType);
    }

    @Override
    public void dealWithMessage(Message<Integer> message) {
        uiHandler.obtainMessage(1, "Command received: " + message.getPayload()).sendToTarget();
    }

//    @Override
//    public void listenAndExecuteCommands() {
//        threadFactory.newThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Socket socket = futureForSocket.get();
//                    if (socket == null) return;
//
//                    dataStream = new DataInputStream(socket.getInputStream());
//                    uiHandler.obtainMessage(1, "Waiting for data").sendToTarget();
//                    while (true) {
//                        try {
//                            int dataFromSocket = dataStream.read(); // or use readByte and catch EOFException
//                            if (dataFromSocket == -1) {
//                                uiHandler.obtainMessage(1, "End of transmission").sendToTarget();
//                                break;
//                            }
//                            uiHandler.obtainMessage(1, "Command received: " + dataFromSocket).sendToTarget();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            break;
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
}
