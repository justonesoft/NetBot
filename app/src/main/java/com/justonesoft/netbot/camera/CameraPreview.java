package com.justonesoft.netbot.camera;

/**
 * Created by bmunteanu on 2/22/2016.
 */

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private static final String TAG = "CAMERA_PREVIEW";
    private Camera.Size minSize = null;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {

            mCamera.stopPreview();
            Camera.Parameters params = mCamera.getParameters();
//            mCamera.setDisplayOrientation(90);
            params.setRotation(90);
//            setRotation(params);
//            mCamera.setParameters(params);
//
//            params = mCamera.getParameters();
//            params.setRotation(270);
            List<Camera.Size> acceptedSizes = params.getSupportedPictureSizes();
            determinePictureFormat(params);
            determineOptimumImageSize(acceptedSizes, false);


            Log.i(TAG, "Min Size is: w-" + minSize.width + " : h-" + minSize.height);


            params.setColorEffect(android.hardware.Camera.Parameters.EFFECT_MONO);
//            params.setPictureSize(Math.min(minSize.width, minSize.height), Math.max(minSize.width, minSize.height));
            params.setPictureSize(minSize.width, minSize.height);
            mCamera.setParameters(params);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    private void determinePictureFormat(Camera.Parameters params) {
        List<Integer> supportedImageFormats = params.getSupportedPictureFormats();
        for (Integer pictureFormat : supportedImageFormats) {
            Log.i(TAG, "ImageFormat: " + pictureFormat);
        }
    }

    private void determineOptimumImageSize(List<Camera.Size> acceptedSizes, boolean useMinim) {
        // select the minimum image size for portrait: width < height and width x height is minim
        long surface = Long.MAX_VALUE;
        Collections.sort(acceptedSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return (lhs.width*lhs.height) - (rhs.width*rhs.height);
            }
        });
        if (useMinim) {
            minSize = acceptedSizes.get(0);
        }
        else {
            if (acceptedSizes.size() >= 3) minSize = acceptedSizes.get(2);
            else if (acceptedSizes.size() >= 2) minSize = acceptedSizes.get(1);
            else minSize = acceptedSizes.get(0);
        }
        /*
        for (Camera.Size cSize: acceptedSizes) {
           Log.i(TAG, "Size: w-" + cSize.width + " : h-" + cSize.height);
           if ((cSize.width * cSize.height) < surface) {
                surface = cSize.width * cSize.height;
                minSize = cSize;
           }
        }
        */
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        mCamera.stopPreview();

        // set preview size and make any resize, rotate or
        // reformatting changes here

//            mCamera.setDisplayOrientation(90); // this will fail if activity is explicitly set
        // to a specific orientation in AndroidManifest.xml or handles orientation changes
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(90);
//            setRotation(params);
        params.setColorEffect(android.hardware.Camera.Parameters.EFFECT_MONO);
//        mCamera.setParameters(params);
//
//        params = mCamera.getParameters();
//        params.setRotation(270);
        if (minSize != null) {
//            params.setPictureSize(Math.min(minSize.width, minSize.height), Math.max(minSize.width, minSize.height));
            params.setPictureSize(minSize.width, minSize.height);
        } else {
            List<Camera.Size> acceptedSizes = params.getSupportedPreviewSizes();

            // select the minimum image size for portrait: width < height and width x height is minim
            determinePictureFormat(params);
            determineOptimumImageSize(acceptedSizes, false);
//            params.setPictureSize(Math.min(minSize.width, minSize.height), Math.max(minSize.width, minSize.height));
            params.setPictureSize(minSize.width, minSize.height);

            Log.i(TAG, "Min Size is: w-" + minSize.width + " : h-" + minSize.height);
        }
        mCamera.setParameters(params);

        // connect preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void setRotation(Camera.Parameters params) {
        Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        int orientation = info.orientation;
        if (orientation == -1) {
            Log.i(TAG, "ORIENTATION_UNKNOWN");
            return;
        }

        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;
        rotation = (info.orientation + orientation) % 360;
        Log.i(TAG, "Rotation: " + rotation);
        params.setRotation(rotation);
    }
}
