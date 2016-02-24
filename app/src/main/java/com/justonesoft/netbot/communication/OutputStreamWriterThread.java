package com.justonesoft.netbot.communication;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by bmunteanu on 2/16/2016.
 */
public class OutputStreamWriterThread {
    private OutputStream outputStream;

    public OutputStreamWriterThread(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(Byte command) {
        try {
            // write to bluetooth
            outputStream.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
