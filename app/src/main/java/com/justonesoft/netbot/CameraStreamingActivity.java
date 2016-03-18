package com.justonesoft.netbot;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.justonesoft.netbot.camera.CameraManager;
import com.justonesoft.netbot.camera.CameraPreview;
import com.justonesoft.netbot.camera.ImageStreamer;
import com.justonesoft.netbot.communication.SocketFactory;
import com.justonesoft.netbot.communication.StreamingThread;
import com.justonesoft.netbot.util.StatusTextUpdater;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.logging.LogRecord;

public class CameraStreamingActivity extends ActionBarActivity implements StatusTextUpdater {

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView statusText;

    private boolean streaming = false;
    private boolean canStartPreview = false;
    private Object synchronizer = new Object();

    private AsyncTask<Void, Void, Void> streamingTask = null;

    public static int TEXT_UPDATE_ID = StatusTextUpdaterManager.nextId();
    private static final int AIMED_FPS = 20;

    private ImageStreamer imageStreamer;
    private StreamingThread streamingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LIFE_FLOW", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_streaming);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        StatusTextUpdaterManager.registerTextUpdater(TEXT_UPDATE_ID, this);

        // Create an instance of Camera
        mCamera = CameraManager.getCameraInstance();

        ToggleButton toggle = (ToggleButton) findViewById(R.id.streaming_button);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && mCamera != null) {
                    // aim for 1 fps
                    streamingTask = prepareForStreaming();
                    if (streamingTask != null) {
                        streamingTask.execute((Void) null);
                    }
                } else {
                    stopStreaming();
                }
            }
        });
    }

    private AsyncTask<Void, Void, Void> prepareForStreaming() {
        // get the Socket and its OutputStream -- needs to be done in an AsyncTask, connecting sockets is not allowed on the main/UI thread
        Socket socket = SocketFactory.getConnectedSocket();
        if (socket == null) {
            Log.e("CONNECTION", "Could not create socket");
            return null;
        }
        // create the ImageStreamer
        try {
            imageStreamer = new ImageStreamer(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // create the StreamingThread if not already created
        if (streamingThread == null) {
            streamingThread = new StreamingThread();
        }

        streamingThread.startStreaming();

        //set the booleans accordingly
        streaming = true;
        canStartPreview = true;

        final Handler handler = new Handler();

        // if all above is OK create the AsyncTask
        streamingTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (streaming && !isCancelled()) {

                    synchronized (synchronizer) {
                        while (!canStartPreview && streaming && !isCancelled()) {
                            try {
                                synchronizer.wait(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (streaming && !isCancelled()) {
                        mCamera.startPreview();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                    @Override
                                    public void onPictureTaken(byte[] data, Camera camera) {
                                        try {
                                            if (data != null) {
                                                Log.i("PICTURE_TAKEN", "Image size: " + data.length);

                                                if (streamingThread != null) {
                                                    streamingThread.stream(data, imageStreamer);
                                                }
                                            }
                                        }
                                        finally {
                                            canStartPreview = true;
                                            synchronized (synchronizer) {
                                                synchronizer.notify();
                                            }
                                        }
                                    }
                                });
                            }
                        }, 1000 / AIMED_FPS);
                        canStartPreview = false;
                    }
                }

                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }
        };

        return streamingTask;
    }

    private void stopStreaming() {
        // set streaming boolean to false
        streaming = false;

        // cancel the task in case it is still running
        if (streamingTask != null) {
            // cancel asynctask
            streamingTask.cancel(false);
        }

        // close the Socket
        // destroy the image streamer
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LIFE_FLOW", "onResume");

        this.statusText = (TextView) findViewById(R.id.conn_status);

        if (mCamera == null) {
            updateStatusText("Camera not available");
        } else {
            // Create our Preview view and set it as the content of our activity.
            if (mPreview == null) {
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview_frame);
                preview.addView(mPreview);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("LIFE_FLOW", "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LIFE_FLOW", "onStop");
        stopStreaming();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("LIFE_FLOW", "onRestoreInstanceState");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("LIFE_FLOW", "onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        Log.d("LIFE_FLOW", "onDestroy");
        super.onDestroy();
        stopStreaming();
        if (mCamera != null) {
            mCamera.release();
            if (mPreview != null) {
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview_frame);
                if (preview != null) {
                    preview.removeView(mPreview);
                }
            }
        }
    }

    @Override
    public void updateStatusText(int statusType, Object payload) {

    }

    @Override
    public void updateStatusText(int statusType) {

    }

    @Override
    public void updateStatusText(String statusText) {
        if (this.statusText != null) {
            this.statusText.setText(statusText);
        }
    }
}
