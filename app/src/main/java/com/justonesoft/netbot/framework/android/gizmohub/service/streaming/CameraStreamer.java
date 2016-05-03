package com.justonesoft.netbot.framework.android.gizmohub.service.streaming;

import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.justonesoft.netbot.camera.CameraManager;
import com.justonesoft.netbot.camera.CameraPreview;
import com.justonesoft.netbot.framework.android.gizmohub.service.StreamingDataReadyListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by bmunteanu on 4/15/2016.
 */
public class CameraStreamer implements Streamer<byte[]> {
    public static final int IMAGE_BUFFER_SIZE = 120000;
    private ViewGroup cameraSurface;
    private Camera camera;
    private CameraPreview cameraPreview;
    private CameraStreamingAsyncTask cameraStreamingTask;
    private StreamingDataReadyListener dataReadyListener;
    private ByteBuffer imageSizeBuffer;
    private ByteBuffer imageBuffer;
    private byte frame = 0;

    private BlockingDeque<FrameForImage> streamingDeque = new LinkedBlockingDeque<>(1);

    public CameraStreamer(ViewGroup cameraSurface) {
        this.cameraSurface = cameraSurface;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        if (outputStream == null) throw new NullPointerException("Provided OutputStream is null.");
    }

    @Override
    public boolean prepare() {
        // Create an instance of Camera
        camera = CameraManager.getCameraInstance();

//        setCameraDisplayOrientation();
        cameraSurface.addView(cameraPreview = new CameraPreview(cameraSurface.getContext(), camera));
        imageSizeBuffer = ByteBuffer.allocate(4); //integer  = 4 bytes
        imageBuffer = ByteBuffer.allocate(IMAGE_BUFFER_SIZE);
//        camera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
////                Log.i("PreviewCallback", "Size: " + data.length);
//                try {
//                    stream(data);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        return false;
    }

    @Override
    public void startStreaming() {
        camera.startPreview();

        cameraStreamingTask = new CameraStreamingAsyncTask(this);

        cameraStreamingTask.execute((Void) null);

        Thread runner = new Thread(new Runnable() {
            @Override
            public void run() {
                FrameForImage ffi = null;
                while (cameraStreamingTask != null && !cameraStreamingTask.isCancelled()) {
                    try {
                        ffi = streamingDeque.pollFirst(2, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (ffi == null || ffi.imageData == null || ffi.imageData.length == 0) continue;

                    if (cameraStreamingTask == null || cameraStreamingTask.isCancelled()) return;

                    Log.i("OFFER_FRAME", "Take Frame: " + ffi.frame);

//                    imageBuffer.rewind();
//                    imageBuffer.putInt(ffi.imageData.length);
//                    imageBuffer.put(ffi.frame);
//                    imageBuffer.put(ffi.imageData);
//                    byte[] data = new byte[imageBuffer.position()];
//                    imageBuffer.rewind();
//                    imageBuffer.get(data);
//                    dataReadyListener.streamingDataReady(data);
                    imageSizeBuffer.rewind();
                    imageSizeBuffer.putInt(ffi.imageData.length);

                    dataReadyListener.streamingDataReady(imageSizeBuffer);

                    int remaining = ffi.imageData.length;
                    int head = 0;
                    int chunk = remaining > IMAGE_BUFFER_SIZE ? IMAGE_BUFFER_SIZE : remaining % IMAGE_BUFFER_SIZE;
                    imageBuffer.limit(IMAGE_BUFFER_SIZE);
                    imageBuffer.rewind();
                    imageBuffer.put(ffi.frame);
                    while (remaining > 0) {
                        imageBuffer.put(ffi.imageData, head, chunk);
                        dataReadyListener.streamingDataReady(imageBuffer);
                        remaining = remaining - chunk;
                        head += chunk;
                        chunk = remaining > IMAGE_BUFFER_SIZE ? IMAGE_BUFFER_SIZE : remaining % IMAGE_BUFFER_SIZE;
                        imageBuffer.rewind();
                    }
                }
            }
        });
        runner.start();
    }

    @Override
    public boolean terminate() {
        // TODO check for null
        if (cameraStreamingTask != null) {
            cameraStreamingTask.cancel(false);
        }
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
        }
        camera = null;
//        if (cameraPreview != null && cameraSurface != null) {
//            cameraSurface.removeView(cameraPreview);
//        }
        cameraStreamingTask = null;
        return true;
    }

    @Override
    public void stream(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length == 0) return;
        if (dataReadyListener == null) throw new NullPointerException("DataReadyListener is null. Must call setOutputStream method.");

        frame++;
        if (frame > 100) frame = 0;

        FrameForImage ffi = new FrameForImage();
        ffi.frame = frame;
        ffi.imageData = imageData;
        boolean offered = streamingDeque.offerFirst(ffi);
        Log.i("OFFER_FRAME", "Offer Frame: " + ffi.frame + " - " + offered);
    }

    @Override
    public void releaseStream() {

    }

    public Camera getCamera() {
        return camera;
    }

    @Override
    public void setStreamingDataReadyListener(StreamingDataReadyListener dataReadyListener) {
        this.dataReadyListener = dataReadyListener;
    }

    public void setCameraDisplayOrientation() {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);
        int rotation = ((WindowManager)this.cameraSurface.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    class FrameForImage {
        byte[] imageData;
        byte frame;
    }
}

