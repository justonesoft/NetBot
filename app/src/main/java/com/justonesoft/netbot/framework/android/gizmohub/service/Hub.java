package com.justonesoft.netbot.framework.android.gizmohub.service;

import android.os.Handler;

import com.justonesoft.netbot.camera.ImageStreamer;

import java.util.concurrent.Future;

/**
 * Created by bmunteanu on 3/31/2016.
 */
public interface Hub {
    public Hub connect();
    public Hub disconnect();
    public boolean isConnected();

    public ImageStreamer giveMeImageStreamer();
    public CommandListener giveMeBluetoothCommandListener(String pairedDeviceName);
    public CommandListener giveMeUICommandListener(Handler uiHandler);
}
