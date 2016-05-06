package com.justonesoft.netbot.framework.android.gizmohub.service;

import java.nio.ByteBuffer;

/**
 * Created by bmunteanu on 4/20/2016.
 */
public interface StreamingDataReadyListener {
    public void streamingDataReady(ByteBuffer dataBuffer);

    public void streamingDataReady(byte[] data);
}

