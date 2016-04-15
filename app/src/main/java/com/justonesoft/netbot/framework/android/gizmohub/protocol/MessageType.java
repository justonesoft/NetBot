package com.justonesoft.netbot.framework.android.gizmohub.protocol;

import android.util.Log;

/**
 * Created by bmunteanu on 4/9/2016.
 */
public enum MessageType {
    BLUETOOTH((byte)1),
    SMS((byte)2),
    UI((byte)3),
    START_STREAM((byte)4);

    private final byte commandByte;

    MessageType(final byte commandByte) {
        this.commandByte = commandByte;
    }

    public byte commandByte() {
        return commandByte;
    }

    public static MessageType fromCommandByte(byte commandByte) {
        Log.d("MessageType", "commandByte: " + commandByte);
        switch (commandByte) {
            case 1:
                return BLUETOOTH;
            case 2:
                return SMS;
            case 3:
                return UI;
            case 4:
                return START_STREAM;
            default:
                return null;
        }
    }
}
