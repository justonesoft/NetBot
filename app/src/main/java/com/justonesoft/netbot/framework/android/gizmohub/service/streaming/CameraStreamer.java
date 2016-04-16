package com.justonesoft.netbot.framework.android.gizmohub.service.streaming;

import android.hardware.Camera;
import android.view.ViewGroup;

import com.justonesoft.netbot.camera.CameraManager;
import com.justonesoft.netbot.camera.CameraPreview;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bmunteanu on 4/15/2016.
 */
public class CameraStreamer implements Streamer<byte[]> {
    private ViewGroup cameraSurface;
    private Camera camera;
    private DataOutputStream outputStream;
    private CameraPreview cameraPreview;

    public CameraStreamer(ViewGroup cameraSurface) {
        this.cameraSurface = cameraSurface;
        // Create an instance of Camera
        camera = CameraManager.getCameraInstance();

    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        if (this.outputStream == null) throw new NullPointerException("Provided OutputStream is null.");
        this.outputStream = new DataOutputStream(outputStream);
    }

    @Override
    public boolean prepare() {
        cameraSurface.addView(cameraPreview = new CameraPreview(cameraSurface.getContext(), camera));
        return false;
    }

    @Override
    public void startStreaming() {
        camera.startPreview();
    }

    @Override
    public boolean terminate() {
        camera.stopPreview();
        if (cameraPreview != null && cameraSurface != null) {
            cameraSurface.removeView(cameraPreview);
        }
        return false;
    }

    @Override
    public void stream(byte[] data) throws IOException {
        if (data == null || data.length == 0) return;
        if (this.outputStream == null) throw new NullPointerException("OutputStream is null. Must call setOutputStream method.");

        this.outputStream.writeInt(data.length);
        this.outputStream.write(data);
    }

    @Override
    public void releaseStream() {

        try {
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.outputStream = null;
    }
}
