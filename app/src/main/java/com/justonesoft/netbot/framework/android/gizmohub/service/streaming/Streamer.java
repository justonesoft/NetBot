package com.justonesoft.netbot.framework.android.gizmohub.service.streaming;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bmunteanu on 4/15/2016.
 */
public interface Streamer<T> {
    public void setOutputStream(OutputStream outputStream);
    public boolean prepare();
    public void startStreaming();
    public boolean terminate();
    public void stream(T data) throws IOException;
    public void releaseStream();
}
