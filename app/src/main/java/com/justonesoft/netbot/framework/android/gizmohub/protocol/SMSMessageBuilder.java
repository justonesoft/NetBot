package com.justonesoft.netbot.framework.android.gizmohub.protocol;

/**
 * Created by bmunteanu on 4/9/2016.
 */
public class SMSMessageBuilder extends MessageBuilder {
    private Message<String> message;

    SMSMessageBuilder() {
        message = new Message<>(MessageType.SMS);
        stage = ReadingStage.READ_PAYLOAD_LENGTH;
    }

    @Override
    public ReadingStage currentStage() {
        switch (stage) {
            case IDLE:
                stage = ReadingStage.READ_PAYLOAD_LENGTH;
                return stage;
            case READ_PAYLOAD_LENGTH:
                stage = ReadingStage.READ_PAYLOAD;
                return stage;
            case READ_PAYLOAD:
                stage = ReadingStage.COMPLETE;
                return stage;
        }
        return null;
    }

    @Override
    public Message<String> getMessage() {
        return message;
    }

    @Override
    public byte[] process(byte[] nextChunk) {
        return new byte[0];
    }
}
