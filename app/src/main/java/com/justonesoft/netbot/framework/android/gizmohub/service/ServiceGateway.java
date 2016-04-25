package com.justonesoft.netbot.framework.android.gizmohub.service;

import android.util.Log;

import com.justonesoft.netbot.framework.android.gizmohub.protocol.Message;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.MessageType;
import com.justonesoft.netbot.framework.android.gizmohub.protocol.ReadingProtocol;
import com.justonesoft.netbot.framework.android.gizmohub.service.streaming.Streamer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bmunteanu on 4/8/2016.
 */
public class ServiceGateway implements MessageReadyListener, StreamingDataReadyListener {
    private static final long THREE_SECONDS = 10000;
    private static final int CHUNK_BYTES_SIZE_TO_READ_FROM_CHANNEL = 4; // how many bytes to try to read at once from SocketChannel

    private final String serverAddress;
    private final int serverPort;
    private ExecutorService executor;
    private Future<SocketChannel> futureForSocket;

    private Collection<CommandListener> registeredListeners = new ArrayList<>();
    private Collection<Streamer> registeredStreamers = new ArrayList<>();

    private Selector selector;

    private boolean disconnect = false;
    private boolean connected = false;

    private SocketChannel socketChannel;

    private final ReadingProtocol readingProtocol;

    public ServiceGateway(String serverAddress, int port) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = port;
        executor = Executors.newSingleThreadExecutor();

        readingProtocol = new ReadingProtocol();
        readingProtocol.setMessageReadyListener(this);

        selector = Selector.open();
    }

    public void registerCommandListener(CommandListener commandListener) {
        if (commandListener == null) return;
        registeredListeners.add(commandListener);
    }

    public void registerStreamer(Streamer streamer) {
        if (streamer == null) return;
        streamer.prepare();
        streamer.setStreamingDataReadyListener(this);
        this.registeredStreamers.add(streamer);
    }

    public void startStreaming() {
        for (Streamer streamer : registeredStreamers) {
            streamer.startStreaming();
        }
    }

    public void stopStreaming() {
        for (Streamer streamer : registeredStreamers) {
            streamer.terminate();
        }
    }

    public void connect() {
        disconnect = false;
        final ByteBuffer buffer = ByteBuffer.allocate(CHUNK_BYTES_SIZE_TO_READ_FROM_CHANNEL);
        final byte[] bytesRead = new byte[CHUNK_BYTES_SIZE_TO_READ_FROM_CHANNEL];
        final int MAX_RETRIES = 3;

        // try to establish a new connection
        futureForSocket = executor.submit(new Callable<SocketChannel>() {
            @Override
            public SocketChannel call() throws Exception {
                if (connected) return null;
                connected = true; // even it is not yet, consider it is because we don't want some other thread to try connecting
                try {

                    socketChannel = SocketChannel.open();

                    // try to connect
                    int retries = 1;

                    while (retries <= MAX_RETRIES) {
                        try {
                            if (socketChannel.isConnected()) break;
                            socketChannel = SocketChannel.open();
                            socketChannel.socket().connect(new InetSocketAddress(serverAddress, serverPort), (int) THREE_SECONDS * retries);
                        } catch (SocketTimeoutException timeout) {
                            timeout.printStackTrace();
                            retries++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            connected = false;
                            throw e;
                        }
                    }

                    if (!socketChannel.isConnected()) {
                        connected = false;
                        Log.d("ServiceGateway", "Could Not Connected !!!");
                        return null;
                    } else {
                        connected = true; //already true but make it more clear
                        Log.d("ServiceGateway", "Connected !!!");
                    }

                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    while (!disconnect) {
                        int selCount = selector.select(THREE_SECONDS);
                        if (selCount == 0) continue;
                        Iterator<SelectionKey> selectedKeysIterator = selector.selectedKeys().iterator();
                        while (selectedKeysIterator.hasNext()) {
                            SelectionKey readyOperationKey = selectedKeysIterator.next();
                            selectedKeysIterator.remove();
                            if (readyOperationKey.isValid() && readyOperationKey.isReadable()) {
                                // now read from this
                                buffer.rewind();
                                int size = socketChannel.read(buffer);

                                if (size > 0) {
                                    buffer.rewind();
                                    buffer.get(bytesRead, 0, size);
                                    readingProtocol.process(bytesRead, size);
                                }
                            }
                        }
                    }
                    socketChannel.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
                finally {
                    disconnect = true;
                    if (socketChannel != null) {
                        socketChannel.close();
                    }
                    socketChannel = null;
                    connected = false;
                }
                return socketChannel;
            }
        });
    }

    public void disconnect() {
        disconnect = true;
        stopStreaming();
    }

    @Override
    public void messageReady(Message message) {
        for (CommandListener commandListener : registeredListeners) {
            if (commandListener.isInterestedIn(message.getType())) {
                commandListener.dealWithMessage(message);
            }
        }
    }

    @Override
    public void streamingDataReady(ByteBuffer dataBuffer) {
        dataBuffer.flip();
        try {
            while (dataBuffer.hasRemaining()) {
                socketChannel.write(dataBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
