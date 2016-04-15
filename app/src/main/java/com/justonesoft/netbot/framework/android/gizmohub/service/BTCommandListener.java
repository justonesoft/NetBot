package com.justonesoft.netbot.framework.android.gizmohub.service;

import android.bluetooth.BluetoothDevice;
import com.justonesoft.netbot.bt.BTController;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.Message;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.MessageType;

/**
 * Created by bmunteanu on 4/6/2016.
 */
public class BTCommandListener implements CommandListener<Byte> {

    public BTCommandListener(String pairedDeviceName) {
        BluetoothDevice btDevice = BTController.getInstance().getPairedBTDeviceByName(pairedDeviceName);
        if (btDevice == null) return; // or throw exception?

        // connect with the device
        BTController.getInstance().connectWithBTDevice(btDevice);
    }

    @Override
    public boolean isInterestedIn(MessageType messageType) {
        return MessageType.BLUETOOTH.equals(messageType);
    }

    @Override
    public void dealWithMessage(Message<Byte> message) {
        if (message ==  null) return;
        if (isInterestedIn(message.getType())) {
            BTController.getInstance().sendCommand(message.getPayload());
        }
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
//                    while (true) {
//                        try {
//                            int dataFromSocket = dataStream.read(); // or use readByte and catch EOFException
//                            if (dataFromSocket == -1) {
//                                break;
//                            }
//                            BTController.getInstance().sendCommand(Byte.valueOf((byte) dataFromSocket));
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
