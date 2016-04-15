package com.justonesoft.netbot.framework.android.gizmohub.service;

import com.justonesoft.netbot.framework.android.gizmohub.protocol.Message;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.MessageType;

/**
 * Created by bmunteanu on 3/31/2016.
 */
public interface CommandListener<T> {

    /**
     * query the listener if it is interested in a message of that type
     * @param messageType
     * @return true / false
     */
    public boolean isInterestedIn(MessageType messageType);

    /**
     * do something with this message
     * @param message
     */
    public void dealWithMessage(Message<T> message);
}
