package com.justonesoft.netbot;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.justonesoft.netbot.camera.CameraManager;
import com.justonesoft.netbot.camera.CameraPreview;
import com.justonesoft.netbot.util.StatusTextUpdater;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;

public class CameraStreamingActivity extends ActionBarActivity implements StatusTextUpdater {

    private Camera mCamera;
    private CameraPreview mPreview;
    private TextView statusText;
    public static int TEXT_UPDATE_ID = StatusTextUpdaterManager.nextId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_streaming);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create an instance of Camera
        mCamera = CameraManager.getCameraInstance();

        StatusTextUpdaterManager.registerTextUpdater(TEXT_UPDATE_ID, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.statusText = (TextView) findViewById(R.id.conn_status);

        if (mCamera == null) {
            updateStatusText("Camera not available");
        } else {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview_frame);
            preview.addView(mPreview);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
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
