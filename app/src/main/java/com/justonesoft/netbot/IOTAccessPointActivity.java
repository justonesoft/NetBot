package com.justonesoft.netbot;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.justonesoft.netbot.framework.gizmohub.service.Hub;
import com.justonesoft.netbot.framework.gizmohub.service.SocketHub;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class IOTAccessPointActivity extends ActionBarActivity {

    private Socket socket;

    /**
     *
     * can be idle(sleeping) or listening for command
     */
    private byte state;

    private Camera camera;

    private boolean isSocketConnected = false;
    private boolean isBluetoothConnected = false;

    private final static String BLUETOOTH_DEVICE_NAME = "HC-05";
    private final static String HUB_SERVER_NAME = "10.45.51.208";
    private final static int HUB_SERVER_PORT = 9999;

    private TextView status_text_view;

    SocketHub hub = new SocketHub(HUB_SERVER_NAME, HUB_SERVER_PORT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iotaccess_point);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status_text_view = (TextView) findViewById(R.id.status_text);
        status_text_view.setMovementMethod(new ScrollingMovementMethod());

        // now we try to connect to server
        status_text_view.setText("Trying to connect...\n");

        new AsyncTask<Hub, Void, Hub>() {

            @Override
            protected Hub doInBackground(Hub... params) {
                Hub hub = params[0];
                Future<Hub> hubFuture = hub.connect();
                try {
                    return hubFuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            };

            @Override
            protected void onPostExecute(Hub hub) {
                if (hub.isConnected()) {
                    status_text_view.setText("Connected...\n");
                } else {
                    status_text_view.setText("Could not connect...\n");
                }
            }
        }.execute(hub);

//        status_text_view.post(new Runnable() {
//            public void run() {
//                status_text_view.scrollTo(0, status_text_view.getBottom());
//            }
//        });
    }

    private String createDummyText() {
        String s = "";

        for (int i=0; i<7; i++) {

            s += "Line " + i + "\n";
        }

        return s;
    }

    /**
     * Try to connect to the Hub Server. <br />
     * Should be called inside a AsyncTask.
     *
     */
    private void connectToServer(String serverAddress, int serverPort) {
        isSocketConnected = false;
    }

    /**
     * Try to connect to the Bluetooth Device. <br />
     * Should be called inside a AsyncTask.
     *
     */
    private void connectToBluetooth(String bluetoothDeviceName) {
        isBluetoothConnected = true;
    }

    private void startCameraPreview(Camera camera) {

    }

    private void disconnectAndReleaseCamera() {
        if (camera != null) {
            camera.release();
        }
        camera = null; //not sure about this
    }

    private void disconnectFromSocket() {
        isSocketConnected = false;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket = null;
    }

    private void disconnectFromBluetooth() {
        isBluetoothConnected = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disconnectFromSocket();
        disconnectFromBluetooth();
        disconnectAndReleaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

        disconnectFromSocket();
        disconnectFromBluetooth();
        disconnectAndReleaseCamera();
    }
}
