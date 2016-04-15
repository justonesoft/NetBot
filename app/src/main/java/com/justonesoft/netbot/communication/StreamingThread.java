package com.justonesoft.netbot.communication;

import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class is implemented so that only one streaming flow is active at a time.
 * Created by bmunteanu on 3/14/2016.
 */
public class StreamingThread {

    private static final int MAX_START_STREAMING_RETRIES = 3;
    private static final int secondsToWaitForNewStreamingFlow = 1;

    private BlockingQueue<Payload> dataQueue = new LinkedBlockingQueue<Payload>(2); // only 2 "frames" so that we don't use too much memory
    private static boolean streaming = false;
    private static boolean currentStreamingRunning = false;

    private static final StreamingThread instance = new StreamingThread();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (streaming || !dataQueue.isEmpty()) {
                currentStreamingRunning = true;
                // wait for one second for data to be available
                try {
                    Payload payload = dataQueue.poll(1, TimeUnit.SECONDS);
                    if (payload == null) continue; // no data in the queue to stream

                    // add this data to the streamer
                    Object data = payload.getData();
                    Streamer streamer = payload.getStreamer();
                    try {
                        streamer.stream(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // TODO don't ignore exceptions
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            currentStreamingRunning = false;
        }
    };

    public static StreamingThread getInstance() {
        return instance;
    }

    /**
     * There is no guarantee that the data will be streamed. One failure reason can be because the capacity of internal blocking queue is reached.
     * @param data the data to be streamed by stramer
     * @param streamer the streamer used to stream tha data
     *
     * @return true or false if the data has been pushed for streming or not
     */
    public <E, S extends Streamer<E>> boolean stream (E data, S streamer) {
        if (!streaming) return false;
        Payload<E, S> payload = new Payload<E, S>(data, streamer);
        return dataQueue.offer(payload);
    }


    public void stopStreaming() {
        this.streaming = false;
    }

    public boolean startStreaming() {
        if (streaming || currentStreamingRunning) return false; //already streaming

        // connect a thread with the runnable
        new Thread(runnable).start();
        streaming = true;

        return true;
    }

    private class Payload<E, S extends Streamer<E>> {
        private E data;
        private S streamer;

        Payload (E data, S streamer) {
            this.data = data;
            this.streamer = streamer;
        }

        E getData() {
            return data;
        }

        S getStreamer() {
            return streamer;
        }
    }
}
