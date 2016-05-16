package com.justonesoft.netbot.camera;

import com.justonesoft.netbot.communication.Streamer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <em></em>Currently not supported by the server. </em><br>
 * Server protocol is: 4bytes - image size; 1 byte - frame number; byte[size] - image data bytes
 * Created by bmunteanu on 3/10/2016.
 */
public class ImageStreamer implements Streamer<byte[]> {
    private final DataOutputStream outputStream;

    private boolean ready = false;

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
