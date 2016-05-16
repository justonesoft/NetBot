package com.justonesoft.netbot.framework.android.gizmohub.service.streaming;

import com.justonesoft.netbot.framework.android.gizmohub.service.StreamingDataReadyListener;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bmunteanu on 4/15/2016.
 */
public interface Streamer<T> {
    public boolean prepare();
    public void startStreaming();
    public void stopStreaming();
    public boolean terminate();
    public void stream(T data) throws IOException;

    void setStreamingDataReadyListener(StreamingDataReadyListener dataReadyListener);
}
