package com.justonesoft.netbot.communication;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by bmunteanu on 2/16/2016.
 */
public class InputStreamReaderThread extends Thread {
    private final InputStream inputStream;
    private final BluetoothCommunicator communicator;

    private final int BUFFER_SIZE = 50;
    private final byte[] internalBuffer = new byte[BUFFER_SIZE];

    public InputStreamReaderThread(InputStream inputStream, BluetoothCommunicator communicator) {
        this.inputStream = inputStream;
        this.communicator = communicator;
    }

    @Override
    public void run() {
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                final int bytesRead = inputStream.read(internalBuffer);

                // Send the obtained bytes to the UI activity
                communicator.process(internalBuffer, bytesRead);

            } catch (IOException e) {
                break;
            }
        }
    }
}
