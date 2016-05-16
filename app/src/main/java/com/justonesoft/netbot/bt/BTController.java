package com.justonesoft.netbot.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import com.justonesoft.netbot.NavigateActivity;
import com.justonesoft.netbot.communication.BluetoothCommunicator;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.Message;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.MessageType;
import com.justonesoft.netbot.framework.android.gizmohub.service.CommandListener;
import com.justonesoft.netbot.util.Commands;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;
import com.justonesoft.netbot.util.StatusUpdateType;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by bmunteanu on 4/22/2015.
 */
public class BTController implements CommandListener<Byte> {

    public static final int BT_STATUS = 1000;

    private static BTController instance = new BTController();
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket;

    private BluetoothCommunicator communicator;

    private String DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    private BTController() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BTController getInstance() {
        return instance;
    }

    /**
     * Check if the current device has Bluetooth capability
     * @return true, false
     */
    public boolean deviceHasBluetooth() {
        return btAdapter != null;
    }

    /**
     * Check if Bluetooth is enabled or not.
     * @return true, false
     */
    public boolean isEnabled() {
        if (!deviceHasBluetooth()) {
            return false;
        }
        return btAdapter.isEnabled();
    }

    /**
     * Return the list of all paired devices for this device.
     *
     * @return Set&lt;BluetoothDevice&gt;
     */
    public Set<BluetoothDevice> getPairedDevices() {
        if (!isEnabled()) {
            return null; // or throw exception??
        }
        return btAdapter.getBondedDevices();
    }

    /**
     * Looks through the list of Bluetooth paired devices and returns the {android.bluetooth.BluetoothDevice} with that address.
     * @param deviceAddress - the device address
     *
     * @return Paired BLuetoothDevice with that address or null if no paired devices with that address is found
     */
    public BluetoothDevice getPairedBTDeviceByAddress(String deviceAddress) {
        // look for the actual BluetoothDevice
        Set<BluetoothDevice> pairedDevices = getPairedDevices();

        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getAddress().equals(deviceAddress)) {
                return bt;
            }
        }
        return null;
    }

    /**
     * Looks through the list of Bluetooth paired devices and returns the {android.bluetooth.BluetoothDevice} with that address.
     * @param deviceName - the device address
     *
     * @return Paired BLuetoothDevice with that address or null if no paired devices with that address is found
     */
    public BluetoothDevice getPairedBTDeviceByName(String deviceName) {
        // look for the actual BluetoothDevice
        Set<BluetoothDevice> pairedDevices = BTController.getInstance().getPairedDevices();

        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getName().equals(deviceName)) {
                return bt;
            }
        }
        return null;
    }

    public void connectWithBTDevice(BluetoothDevice connectedDevice) {
        connectWithBTDevice(connectedDevice, DEFAULT_UUID);
    }

    /**
     * Init BT connection
     * @param connectedDevice
     * @return
     */
    public void connectWithBTDevice(BluetoothDevice connectedDevice, String uuidString) {
        if (isConnected()) {
            // already connected
            // send a message to handler to update the UI with a message
            Toast.makeText(null, "bla", Toast.LENGTH_SHORT).show();
            StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, BT_STATUS, StatusUpdateType.BT_STATUS_CONNECTED);
            return;
        }

        BluetoothSocket btSocket = null;
        try {
            btSocket = connectedDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuidString));
            // we need to connect in a separate thread because the connect method is blocking the thread
            BluetoothConnector connector = new BluetoothConnector(btSocket);
            connector.start(); //connect the connection process
        } catch (IOException e) {
            e.printStackTrace();
            StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, BT_STATUS, StatusUpdateType.BT_STATUS_ERROR_NO_SOCKET);
        }
    }

    public void sendCommand(Byte command) {
        if (isConnected()) {
            communicator.write(command);
        }
    }

    public void cancelDiscovery() {
        if (deviceHasBluetooth()) {
            this.btAdapter.cancelDiscovery();
        }
    }

    public boolean isConnected() {
        return btSocket != null;
    }

    private void setBtSocket(final BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }

    @Override
    public boolean isInterestedIn(MessageType messageType) {
        return MessageType.BLUETOOTH.equals(messageType);
    }

    @Override
    public void dealWithMessage(Message<Byte> message) {
        if (message ==  null) return;
        byte dataByte = message.getPayload();
        if (    dataByte != Commands.MOVE_FORWARD.getInfo() &&
                dataByte != Commands.MOVE_BACKWARDS.getInfo() &&
                dataByte != Commands.TURN_RIGHT.getInfo() &&
                dataByte != Commands.TURN_LEFT.getInfo() &&
                dataByte != Commands.STOP.getInfo() ) {
            return;
        } else {
            BTController.getInstance().sendCommand(message.getPayload());
        }
    }

    /**
     * This thread class will be used to connect to the Bluetooth device and obtain the Bluetooth socket used for read/write data.
     */
    class BluetoothConnector extends Thread {
        private BluetoothSocket tmpSocket = null;

        BluetoothConnector (BluetoothSocket socket) {
            tmpSocket = socket;
       }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                tmpSocket.connect();

                // now save this connected socket to the main BTController
                setBtSocket(tmpSocket);

                // send a message to handler to update the UI with a message
                StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, BT_STATUS, StatusUpdateType.BT_STATUS_CONNECTED);

                // we are now connected
                startCommunication();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                connectException.printStackTrace();
                try {
                    tmpSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                setBtSocket(null);
                // send a message to handler to update the UI with a message
                StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, BT_STATUS, StatusUpdateType.BT_STATUS_ERROR_CONNECT);

                return;
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                this.btSocket.getInputStream().close();
                this.btSocket.getOutputStream().close();
                this.btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.btSocket = null;
                this.communicator = null;
            }
        }
    }

    private void startCommunication() {
        if (isConnected()) {
            if (communicator == null) {
                // initialize a communicator
                BluetoothCommunicator tmpCommunicator = null;
                try {
                    tmpCommunicator = new BluetoothCommunicator(btSocket.getInputStream(), btSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, BT_STATUS, StatusUpdateType.BT_STATUS_ERROR_COMMUNICATION);
                }

                if (tmpCommunicator != null) {
                    this.communicator = tmpCommunicator;
                    this.communicator.start();
                    StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, BT_STATUS, StatusUpdateType.BT_STATUS_COMMUNICATION_READY);
                }
            }
        }
    }

}
