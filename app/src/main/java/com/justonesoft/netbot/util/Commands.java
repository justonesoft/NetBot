package com.justonesoft.netbot.util;

import android.util.Log;

/**
 * Various definitions for the commands available to be sent to the bot to control it.
 * Created by bmunteanu on 2/11/2016.
 */
public enum Commands {
    /**
     * Command to advance forward.
     */
    MOVE_FORWARD((byte) 1),

    /**
     * Command to move in reverse.
     */
    MOVE_BACKWARDS((byte) 2),

    /**
     * Command to turn to the left.
     */
    TURN_LEFT((byte) 3),

    /**
     * Command to turn to the right.
     */
    TURN_RIGHT((byte) 4),

    /**
     * Command to stop whatever it is doing.
     */
    STOP((byte) 0);

    private final byte whatToSend;

    private Commands(byte whatToSend) {
        this.whatToSend = whatToSend;
    }

    public byte getWhatToSend() {
        return this.whatToSend;
    }
}
