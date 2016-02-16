package com.justonesoft.netbot.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.justonesoft.netbot.R;
import com.justonesoft.netbot.util.TextViewUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by bmunteanu on 4/22/2015.
 */
public class BTController {

    private static final int BT_STATUS_CONNECTED = 0;
    private static final int BT_STATUS_COMMUNICATION_READY = 1;
    private static final int BT_STATUS_ERROR_CONNECT = 100;
    private static final int BT_STATUS_ERROR_NO_SOCKET = 101;
    private static final int BT_STATUS_ERROR_COMMUNICATION = 102;


    private static BTController instance = new BTController();
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket;

    private BluetoothCommunicator communicator;

    // this handler will be used to update the connectivity status of bluetooth
    private Handler handler;

    private String DEFAULT_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    private BTController() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // instantiate the handler to use the main thread by passing the main looper
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // this method will actually run on the main UI thread. This is because we have used the mailLooper to instantiate this Handler.
                TextView statusTextView = (TextView) msg.obj;

                if (statusTextView != null) {
                    // update the UI based on the status
                    switch (msg.what) {
                        case BT_STATUS_CONNECTED:
                            TextViewUtil.prefixWithText(statusTextView, statusTextView.getContext().getText(R.string.bluetooth_connected), true);
                            break;
                        case BT_STATUS_COMMUNICATION_READY:
                            TextViewUtil.prefixWithText(statusTextView, statusTextView.getContext().getText(R.string.bluetooth_communication_ready), true);
                            break;
                        case BT_STATUS_ERROR_CONNECT:
                            TextViewUtil.prefixWithText(statusTextView, statusTextView.getContext().getText(R.string.err_could_not_connect_bluetooth), true);
                            break;
                        case BT_STATUS_ERROR_NO_SOCKET:
                            TextViewUtil.prefixWithText(statusTextView, statusTextView.getContext().getText(R.string.err_no_bluetooth_socket), true);
                            break;
                        case BT_STATUS_ERROR_COMMUNICATION:
                            TextViewUtil.prefixWithText(statusTextView, statusTextView.getContext().getText(R.string.err_no_communication), true);
                            break;
                        default:
                            // let the message propagate
                            super.handleMessage(msg);
                    }
                }
            }
        };
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

    public void connectWithBTDevice(BluetoothDevice connectedDevice, TextView statusText) {
        connectWithBTDevice(connectedDevice, statusText, DEFAULT_UUID);
    }

    /**
     * Init BT connection
     * @param connectedDevice
     * @return
     */
    public void connectWithBTDevice(BluetoothDevice connectedDevice, TextView statusText, String uuidString) {
        if (isConnected()) {
            // already connected
            // send a message to handler to update the UI with a message
            handler.obtainMessage(BT_STATUS_CONNECTED, statusText).sendToTarget();
            return;
        }

        BluetoothSocket btSocket = null;
        try {
            btSocket = connectedDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuidString));
            // we need to connect in a separate thread because the connect method is blocking the thread
            BluetoothConnector connector = new BluetoothConnector(btSocket, statusText);
            connector.start(); //start the connection process
        } catch (IOException e) {
            e.printStackTrace();
            handler.obtainMessage(BT_STATUS_ERROR_NO_SOCKET, statusText).sendToTarget();
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

    /**
     * This thread class will be used to connect to the Bluetooth device and obtain the Bluetooth socket used for read/write data.
     */
    class BluetoothConnector extends Thread {
        private BluetoothSocket tmpSocket = null;
        private TextView statusText;

        BluetoothConnector (BluetoothSocket socket, TextView statusText) {
            tmpSocket = socket;
            this.statusText = statusText;
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
                handler.obtainMessage(BT_STATUS_CONNECTED, statusText).sendToTarget();

                // we are now connected
                startCommunication(statusText);
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
                handler.obtainMessage(BT_STATUS_ERROR_CONNECT, statusText).sendToTarget();

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

    private void startCommunication(TextView statusText) {
        if (isConnected()) {
            if (communicator == null) {
                // initialize a communicator
                BluetoothCommunicator tmpCommunicator = null;
                try {
                    tmpCommunicator = new BluetoothCommunicator(btSocket.getInputStream(), btSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(BT_STATUS_ERROR_COMMUNICATION, statusText).sendToTarget();
                }

                if (tmpCommunicator != null) {
                    this.communicator = tmpCommunicator;
                    this.communicator.start();
                    handler.obtainMessage(BT_STATUS_COMMUNICATION_READY, statusText).sendToTarget();
                }
            }
        }
    }

    class BluetoothCommunicator extends Thread {
        // this element is used to receive data from a producer. The data will be read and sent over bluetooth.
        // it is a blocking queue, meaning that it will block until something is ready to be sent over BT
        private BlockingQueue<Byte> commandsToSendQueue = new LinkedBlockingQueue<>(100);

        private InputStream btInputStream;
        private OutputStream btOutputStream;

        public BluetoothCommunicator (InputStream btInputStream, OutputStream btOutputStream) {
            this.btInputStream = btInputStream;
            this.btOutputStream = btOutputStream;
        }

        public void write(Byte command) {
            try {
                commandsToSendQueue.put(command);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public Byte read() {
            // there should be 2 threads: a reader and a writer
            // the reader will pass what it reads to a more specialized unit that actually knows what to do with the data.
            // this specialized unit is also a thread blocking on quea.
            // For example if it is a response to a command or just a plain message.
            return null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // read from queue, blocks if noting is to be read
                    Byte command = commandsToSendQueue.take();

                    // write to bluetooth
                    btOutputStream.write(command);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
