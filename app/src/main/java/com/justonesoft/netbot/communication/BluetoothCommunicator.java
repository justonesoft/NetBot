package com.justonesoft.netbot.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public BluetoothCommunicator(InputStream btInputStream, OutputStream btOutputStream) {
        this.btInputStream = btInputStream;
        this.btOutputStream = btOutputStream;

        writerThread = new OutputStreamWriterThread(btOutputStream);
        readerThread = new InputStreamReaderThread();
    }

    public void write(Byte command) {
        writerThread.write(command);
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
        readerThread.start();
    }
}
