package com.justonesoft.netbot.framework.android.gizmohub.protocol;

/**
 * Created by bmunteanu on 4/9/2016.
 */
public enum ReadingStage {
    IDLE,
    READ_COMMAND,
    READ_PAYLOAD_LENGTH,
    READ_PAYLOAD,
    COMPLETE
}
