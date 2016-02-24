package com.justonesoft.netbot.camera;

import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by bmunteanu on 2/22/2016.
 */
public class CameraManager {
    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){

        }
        return c; // returns null if camera is unavailable
    }

    public static Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = new File("ddd");
            if (pictureFile == null) {
                Log.d("CameraManager", "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("CameraManager", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("CameraManager", "Error accessing file: " + e.getMessage());
            }
        }
    };
}

