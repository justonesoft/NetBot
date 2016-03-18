package com.justonesoft.netbot.camera;

import com.justonesoft.netbot.communication.Streamer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bmunteanu on 3/10/2016.
 */
public class ImageStreamer implements Streamer<byte[]> {
    private final DataOutputStream outputStream;

    private boolean ready = false;

    // needs to have a OutputStream to write the image data
    //    - this OutputStream should come from a Socket.getOutputStream
    // will wait or will be notified when a image data, byte[], is ready for streaming
    //    - if waiting use some BlockingQueue
    //    - if notified, register this as a Listener
    // when new image data is received will try to push it to the outputstream
    //    - will notify some entity when the image has been fully sent

    public ImageStreamer(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
        ready = true;
    }

    public void stream(byte[] imageData) throws Exception{
        ready = false;
        this.outputStream.writeInt(imageData.length);
        this.outputStream.write(imageData);
//        this.outputStream.flush();
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }
}
