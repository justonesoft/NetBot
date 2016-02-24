package com.justonesoft.netbot.communication;

import com.justonesoft.netbot.NavigateActivity;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by bmunteanu on 2/16/2016.
 */
public class InputStreamReaderThread extends Thread {
    private final InputStream inputStream;
    private final BluetoothCommunicator communicator;
    private final InputStreamReader isr;

    private final int BUFFER_SIZE = 50;
    private final byte[] internalBuffer = new byte[BUFFER_SIZE];
    char[] charBuffer = new char[BUFFER_SIZE];

    public InputStreamReaderThread(InputStream inputStream, BluetoothCommunicator communicator) {
        this.inputStream = inputStream;
        this.communicator = communicator;
        isr = new InputStreamReader(inputStream);
    }

    @Override
    public void run() {
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
//                isr.read(charBuffer);
                final int bytesRead = inputStream.read(internalBuffer);

//                char c = (char) charBuffer[0];

                // Send the obtained bytes to the UI activity
                communicator.process(internalBuffer, bytesRead);
//                StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, String.valueOf(charBuffer));

            } catch (IOException e) {
                break;
            }
        }
    }
}
