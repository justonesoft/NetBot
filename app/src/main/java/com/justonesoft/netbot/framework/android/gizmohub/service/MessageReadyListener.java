package com.justonesoft.netbot.framework.android.gizmohub.service;

import com.justonesoft.netbot.framework.android.gizmohub.protocol.Message;

/**
 * Created by bmunteanu on 4/10/2016.
 */
public interface MessageReadyListener {
    public void messageReady(Message message);
}
