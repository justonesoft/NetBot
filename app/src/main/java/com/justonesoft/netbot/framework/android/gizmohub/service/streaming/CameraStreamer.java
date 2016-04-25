package com.justonesoft.netbot.framework.android.gizmohub.service.streaming;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;

import com.justonesoft.netbot.camera.CameraManager;
import com.justonesoft.netbot.camera.CameraPreview;
import com.justonesoft.netbot.framework.android.gizmohub.service.StreamingDataReadyListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by bmunteanu on 4/15/2016.
 */
public class CameraStreamer implements Streamer<byte[]> {
    private ViewGroup cameraSurface;
    private Camera camera;
    private CameraPreview cameraPreview;
    private CameraStreamingAsyncTask cameraStreamingTask;
    private StreamingDataReadyListener dataReadyListener;
    private ByteBuffer imageSizeBuffer;
    private ByteBuffer imageBuffer;

    public CameraStreamer(ViewGroup cameraSurface) {
        this.cameraSurface = cameraSurface;
        // Create an instance of Camera
        camera = CameraManager.getCameraInstance();
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        if (outputStream == null) throw new NullPointerException("Provided OutputStream is null.");
    }

    @Override
    public boolean prepare() {
        cameraSurface.addView(cameraPreview = new CameraPreview(cameraSurface.getContext(), camera));
        imageSizeBuffer = ByteBuffer.allocate(4); //integer  = 4 bytes
        imageBuffer = ByteBuffer.allocate(1024);
        return false;
    }

    @Override
    public void startStreaming() {
        camera.startPreview();

        cameraStreamingTask = new CameraStreamingAsyncTask(this);

        cameraStreamingTask.execute((Void) null);
    }

    @Override
    public boolean terminate() {
        // TODO check for null
        if (cameraStreamingTask != null) {
            cameraStreamingTask.cancel(false);
        }
        if (camera != null) {
            camera.stopPreview();
        }

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

        imageSizeBuffer.rewind();
        imageSizeBuffer.putInt(imageData.length);

        dataReadyListener.streamingDataReady(imageSizeBuffer);

        int remaining = imageData.length;
        int head = 0;
        int chunk = remaining > 1024 ? 1024 : remaining % 1024;
        imageBuffer.limit(1024);
        while (remaining > 0) {
            imageBuffer.rewind();
//            Log.d("STREAMING_IMAGE", "remaining: " + remaining + ", head: " + head + ", chunk: " + chunk);
            imageBuffer.put(imageData, head, chunk);
            dataReadyListener.streamingDataReady(imageBuffer);
            remaining = remaining - chunk;
            head += chunk;
            chunk = remaining > 1024 ? 1024 : remaining % 1024;
        }
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
}
