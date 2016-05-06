package com.justonesoft.netbot.camera;

/**
 * Created by bmunteanu on 2/22/2016.
 */

import android.content.Context;
import android.graphics.ImageFormat;
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
    private Camera.Size minPictureSize = null;
    private Camera.Size minPreviewSize = null;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        setCameraParams();


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
            setCameraParams();
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
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

        setCameraParams();

        // connect preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void determinePictureFormat(Camera.Parameters params) {
        List<Integer> supportedImageFormats = params.getSupportedPictureFormats();
        for (Integer pictureFormat : supportedImageFormats) {
            Log.i(TAG, "ImageFormat: " + pictureFormat);
        }

        List<Integer> supportedPreviewFormats = params.getSupportedPreviewFormats();
        for (Integer pictureFormat : supportedImageFormats) {
            Log.i(TAG, "PreviewFormat: " + pictureFormat);
        }
    }

    private Camera.Size determineOptimumImageSize(List<Camera.Size> acceptedSizes, boolean useMinim, int index) {
        // select the minimum image size for portrait: width < height and width x height is minim
        long surface = Long.MAX_VALUE;
        Collections.sort(acceptedSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return (lhs.width * lhs.height) - (rhs.width * rhs.height);
            }
        });
        if (useMinim) {
            return acceptedSizes.get(0);
        }
        else {
            if (acceptedSizes.size() > index) return acceptedSizes.get(index);
            else if (acceptedSizes.size() > (index-1)) return acceptedSizes.get(index-1);
            else return acceptedSizes.get(0);
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

    private void setCameraParams() {
        Camera.Parameters params = mCamera.getParameters();
        determinePictureFormat(params);

        // picture params
        if (minPictureSize == null) {
            List<Camera.Size> acceptedSizes = params.getSupportedPictureSizes();
            minPictureSize = determineOptimumImageSize(acceptedSizes, true, 0);
        }
        Log.i(TAG, "Min Picture Size is: w-" + minPictureSize.width + " : h-" + minPictureSize.height);
        params.setPictureSize(minPictureSize.width, minPictureSize.height);

        params.setColorEffect(Camera.Parameters.EFFECT_MONO);
        params.setJpegQuality(0);
        params.setJpegThumbnailSize(0, 0);
        params.setJpegThumbnailQuality(0);

        // preview params
        final List<int[]> supportedPreviewFpsRanges = params.getSupportedPreviewFpsRange();

        for (int j=0; j<supportedPreviewFpsRanges.size();j++) {
            int[] supp = supportedPreviewFpsRanges.get(j);

            for (int i=0; i<supp.length; i++) {
                Log.i(TAG, "SupFPS["+j+","+i+"]: " + supp[i]);
            }
        }

        if (supportedPreviewFpsRanges != null) {
            final int[] range = supportedPreviewFpsRanges.get(0);

            Log.i(TAG, "MinFPS: " + range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + " MaxFPS: "+range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);

            params.setPreviewFpsRange(range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                    range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        }

        if (minPreviewSize == null) {
            List<Camera.Size> acceptedPreviewSizes = params.getSupportedPreviewSizes();
            minPreviewSize = determineOptimumImageSize(acceptedPreviewSizes, true, 0);
        }
        params.setPreviewFormat(ImageFormat.NV21);
        Log.i(TAG, "Min Preview Size is: w-" + minPreviewSize.width + " : h-" + minPreviewSize.height);
        params.setPreviewSize(minPreviewSize.width, minPreviewSize.height);

        mCamera.setParameters(params);
    }

    public Camera.Size getMinPictureSize() {
        return minPictureSize;
    }

    public Camera.Size getMinPreviewSize() {
        return minPreviewSize;
    }
}
