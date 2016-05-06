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

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            cameraAvailable = true;
        }
        catch (Exception e){
            Log.d("CameraManager", "Camera open error: " + e.getMessage());
            cameraAvailable = false;
            throw e;
        }
        return c; // returns null if camera is unavailable
    }
}

