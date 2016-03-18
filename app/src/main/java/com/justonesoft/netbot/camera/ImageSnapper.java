package com.justonesoft.netbot.camera;

import android.hardware.Camera;

/**
 * Created by bmunteanu on 3/10/2016.
 */
public class ImageSnapper implements Camera.PictureCallback {

    @Override
     public void onPictureTaken(byte[] data, Camera camera) {
        //imageStreamer.imageDataReady(data);
    }
}
