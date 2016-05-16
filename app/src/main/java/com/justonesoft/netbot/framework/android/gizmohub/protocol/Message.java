package com.justonesoft.netbot.framework.android.gizmohub.protocol;

/**
 * Created by bmunteanu on 4/9/2016.
 */
public class Message<P> {
    private MessageType type;
    private int payloadLength;

    private P payload;

    public Message(MessageType type) {
        this.type = type;
    }

    public Message(MessageType type, int payloadLength) {
        this.type = type;
        this.payloadLength = payloadLength;
    }

    protected void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    protected void setPayload(P payload) {
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public P getPayload() {
        return payload;
    }
}
