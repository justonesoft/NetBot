package com.justonesoft.netbot.framework.android.gizmohub.service.streaming;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.justonesoft.netbot.CameraStreamingActivity;

import java.io.IOException;

/**
 * Created by bmunteanu on 4/19/2016.
 */
public class CameraStreamingAsyncTask extends AsyncTask<Void, Void, Void> {

    private Camera camera;
    private CameraStreamer cameraStreamer;
    private boolean canStartPreview = true;
    final Object synchronizer = new Object();

    public CameraStreamingAsyncTask(CameraStreamer cameraStreamer) {
        if (cameraStreamer == null) throw new NullPointerException("CameraStreamer can not be null");

        this.camera = cameraStreamer.getCamera();
        this.cameraStreamer = cameraStreamer;
    }

    @Override
    protected Void doInBackground(Void... params) {

        while (propertiesNotNull() && !isCancelled()) {

            synchronized (synchronizer) {
                while (!canStartPreview && !isCancelled()) {
                    try {
                        synchronizer.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (propertiesNotNull() && !isCancelled()) {
                camera.startPreview();
                canStartPreview = false; //we are about to take a snapshot

                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            if (data != null) {
                                Log.i("PICTURE_TAKEN", "Image size: " + data.length);
                                if (cameraStreamer != null) {
                                    cameraStreamer.stream(data);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            canStartPreview = true;
                            synchronized (synchronizer) {
                                synchronizer.notify();
                            }
                        }
                    }
                });
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                }, 1000 / AIMED_FPS);
            }
        }

        return null;
    }

    private boolean propertiesNotNull() {
        return camera != null &&
                cameraStreamer != null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        canStartPreview = false;
        camera = null;
        cameraStreamer = null;
    }
}
