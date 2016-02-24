package com.justonesoft.netbot.communication;

import com.justonesoft.netbot.processor.CommunicationProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by bmunteanu on 2/16/2016.
 */
public class BluetoothCommunicator extends Thread {
    // this element is used to receive data from a producer. The data will be read and sent over bluetooth.
    // it is a blocking queue, meaning that it will block until something is ready to be sent over BT
    private BlockingQueue<Byte> commandsToSendQueue = new LinkedBlockingQueue<>(100);

    private InputStream btInputStream;
    private OutputStream btOutputStream;

    private final OutputStreamWriterThread writerThread;
    private final InputStreamReaderThread readerThread;

    private final CommunicationProcessor communicationProcessor;

    public BluetoothCommunicator(InputStream btInputStream, OutputStream btOutputStream) {
        this.btInputStream = btInputStream;
        this.btOutputStream = btOutputStream;

        writerThread = new OutputStreamWriterThread(btOutputStream);
        readerThread = new InputStreamReaderThread(btInputStream, this);

        communicationProcessor = new CommunicationProcessor();
    }

    public void write(Byte command) {
        writerThread.write(command);
    }

    public void process(byte[] data, int dataSize) {
        communicationProcessor.process(Arrays.copyOf(data, dataSize));
    }

    @Override
    public void run() {
        readerThread.start();
    }
}
