package com.justonesoft.netbot.framework.android.gizmohub.protocol;

import android.util.Log;

import com.justonesoft.netbot.framework.android.gizmohub.service.MessageReadyListener;

import java.util.Arrays;

/**
 * This calss is responsible with accepting and interpreting data received from server. <br />
 * Communication protocol is: <br />
 * 1st byte - message type; used to create the MessageBuilder
 * rest of data - are processed by the MessageBuilder
 * Created by bmunteanu on 4/9/2016.
 */
public class ReadingProtocol {

//    private final int PROCESSING_QUEUE_SIZE = 10;
    /**
     * THe current state of reading the data from server
     */
    private ReadingStage stage = ReadingStage.IDLE;

    /**
     * Current MesageBuilder responsible with parsing the data from server according to it implementation
     */
    private MessageBuilder messageBuilder;

    /**
     * The listener to be notified when the message is ready
     */
    private MessageReadyListener messageReadyListener;

    /**
     * After data is read from SocketChannel, the bytes will be added to this BlockingQueue.
     */
//    private final BlockingQueue<byte[]> processQueue = new LinkedBlockingQueue<byte[]>();

    /**
     * This thread will read from the queue the bytes composing the image and will process them so that the full image can be received.
     */
//    private Thread reader = new Thread(new Runnable() {
//
//        public void run() {
//            while (true) { // TODO maybe not true, leave room for terminating
//                byte[] nextChunk = null;
//                try {
//
//                    nextChunk = processQueue.take(); // blocks until data is available or interrupted
//                    processReadChunk(nextChunk);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    });

    private void processReadChunk(byte[] nextChunk) {
        if (messageBuilder != null) {
            stage = messageBuilder.currentStage();
        }
        Log.d("ReadingProtocol", "stage: " + stage);
        switch (stage) {
            case IDLE:
                //this is the start of a new message
                if (nextChunk == null || nextChunk.length == 0) return;
                // first byte will represent the command and it is used to create and appropriate MessageBuilder
                MessageType mt = MessageType.fromCommandByte(nextChunk[0]);
                Log.d("ReadingProtocol", "message-type: " + mt);

                messageBuilder = MessageBuilder.create(mt);
                if (nextChunk.length == 1) {
                    stage = messageBuilder.currentStage();
                    return; // no more bytes to process
                }
                nextChunk = Arrays.copyOfRange(nextChunk, 1, nextChunk.length);
                nextChunk = messageBuilder.process(nextChunk);
                stage = messageBuilder.currentStage();
                processReadChunk(nextChunk);
                break;
            case COMPLETE:
                fireMessageReady(messageBuilder.getMessage()); // maybe put in a separate thread
                stage = ReadingStage.IDLE;
                messageBuilder = null;
                if (nextChunk == null || nextChunk.length == 0) return;
                processReadChunk(nextChunk); //basically we haven't altered nextChunk for this stage
                                                // but we have set the stage to be IDLE again
                break;
            default:
                nextChunk = messageBuilder.process(nextChunk);
                stage = messageBuilder.currentStage();
                processReadChunk(nextChunk);
        }
    }

    private void fireMessageReady(Message message) { // maybe this should be in a separate thread not to block the next processing
        if (messageReadyListener != null) {
            messageReadyListener.messageReady(message);
        }
    }

    public ReadingProtocol() {
//        reader.start();

        // define and start the thread that reads from the blocking queue
        // reading protocol:
        //if stage == idle : next byte is the command type
                        // : MessageBuilder.create(type);
                        // : internal stage set to MessageBuilder.currentStage()
        // elseif stage == complete : notify listeners with MessageBuilder.createMessage()
        // else feed the data into MessageBuilder.process
    }

    public void process (byte[] data, int dataSize) {
        byte[] restCopy = new byte[dataSize];
        System.arraycopy(data, 0, restCopy, 0, dataSize);
        // non-blocking push in the reading queue
        processReadChunk(restCopy);
    }

    public void setMessageReadyListener(MessageReadyListener messageReadyListener) {
        this.messageReadyListener = messageReadyListener;
    }
}
