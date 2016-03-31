package com.justonesoft.netbot.framework.gizmohub.service;

import com.justonesoft.netbot.camera.ImageStreamer;

import java.util.concurrent.Future;

/**
 * Created by bmunteanu on 3/31/2016.
 */
public interface Hub {
    public Future<Hub> connect();
    public Hub disconnect();
    public boolean isConnected();

    public ImageStreamer giveMeImageStreamer();
    public CommandListener giveMeBluetoothCommandListener(String pairedDeviceName);
}
