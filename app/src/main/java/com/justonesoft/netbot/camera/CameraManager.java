package com.justonesoft.netbot.camera;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.justonesoft.netbot.CameraStreamingActivity;
import com.justonesoft.netbot.communication.SocketFactory;
import com.justonesoft.netbot.communication.StreamingThread;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;

import java.io.IOException;

/**
 * Created by bmunteanu on 2/22/2016.
 */
public class CameraManager {

    private static boolean cameraAvailable = false;

    private static boolean previewOn = false;

    private static ImageStreamer streamer = null;

    private static ImageStreamer getStreamer() {
        if (streamer == null) {
            try {
                streamer = new ImageStreamer(SocketFactory.getConnectedSocket().getOutputStream());
                return streamer;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("CameraManager", "Can't get the streamer: " + e.getMessage());
            }
        }
        return streamer; //can be null if exception is thrown above
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            cameraAvailable = true;
        }
        catch (Exception e){
            StatusTextUpdaterManager.updateStatusText(CameraStreamingActivity.TEXT_UPDATE_ID, "Could not open camera");
            Log.d("CameraManager", "Camera open error: " + e.getMessage());
            cameraAvailable = false;
        }
        return c; // returns null if camera is unavailable
    }

    public static Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            previewOn = false;
            Log.d("CameraManager", "Image size: " + data.length);

            new AsyncTask<byte[], Void, Void>(){

                @Override
                protected Void doInBackground(byte[]... params) {
                    StreamingThread.getInstance().stream(params[0], getStreamer());
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
//                    CameraStreamingActivity.streamingSemaphore.release();
                    super.onPostExecute(aVoid);
                }
            }.execute(data);
            previewOn = true;
            // imageStreamer.imageDataReady(data);
        }
    };

    public static void startCameraPreview(Camera camera) {
        if (camera == null) return;
        if (!previewOn) {
            camera.startPreview();
            previewOn = true;
        }
    }

    public static void stopCameraPreview(Camera camera) {
        if (camera == null) return;
        camera.stopPreview();
        previewOn = false;
    }
}

