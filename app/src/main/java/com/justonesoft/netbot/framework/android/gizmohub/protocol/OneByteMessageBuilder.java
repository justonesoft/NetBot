package com.justonesoft.netbot.framework.android.gizmohub.protocol;

import java.util.Arrays;

/**
 * Bluetooth specialized MessageBuilder.
 * <br /> <br />
 *
 * Reads data from byte[]. <br />
 *
 * Protocol: 1 byte - the bluetooth command
 * Created by bmunteanu on 4/9/2016.
 */
public class OneByteMessageBuilder extends MessageBuilder {
    private Message<Byte> message;

    OneByteMessageBuilder() {
        message = new Message<>(MessageType.BLUETOOTH, 1);
        stage = ReadingStage.READ_PAYLOAD;
    }

    @Override
    public ReadingStage currentStage() {
        return stage;
    }

    @Override
    public Message<Byte> getMessage() {
        return message;
    }

    @Override
    public byte[] process(byte[] nextChunk) {
        if (stage == ReadingStage.COMPLETE ||
                nextChunk == null ||
                nextChunk.length == 0) return nextChunk;

        switch (stage) {
            case READ_PAYLOAD:
                stage = ReadingStage.COMPLETE;

                message.setPayload(nextChunk[0]);

                if (nextChunk.length == 1) {
                    return new byte[0];
                } else {
                    return Arrays.copyOfRange(nextChunk, 1, nextChunk.length);
                }
            default:
                return nextChunk;
        }
    }
}
