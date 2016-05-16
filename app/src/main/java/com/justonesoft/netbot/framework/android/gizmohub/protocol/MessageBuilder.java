package com.justonesoft.netbot.framework.android.gizmohub.protocol;

import java.nio.ByteBuffer;

/**
 * Created by bmunteanu on 4/9/2016.
 */
public abstract class MessageBuilder {
    ReadingStage stage;
    ByteBuffer payloadAccumulator;
    ByteBuffer payloadLengthAccumulator;

    MessageBuilder() {}

    public static MessageBuilder create(MessageType type) {
        MessageBuilder builder;
        switch (type) {
            case BLUETOOTH:
                builder = new BTMessageBuilder();
                break;
            case SMS:
                builder = new SMSMessageBuilder();
                break;
            default:
                return null;
        }
        return builder;
    }

    public abstract ReadingStage currentStage();
    public abstract Message getMessage();
    public abstract byte[] process(byte[] nextChunk);
}
