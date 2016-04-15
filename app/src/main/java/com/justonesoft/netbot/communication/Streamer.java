package com.justonesoft.netbot.communication;

/**
 * Created by bmunteanu on 3/15/2016.
 */
public interface Streamer<E> {

    /**
     * Stream the E payload.
     * @param element
     */
    public void stream(E element) throws Exception;
}
