package com.justonesoft.netbot;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.justonesoft.netbot.bt.BTController;
import com.justonesoft.netbot.camera.CameraPreview;
import com.justonesoft.netbot.framework.android.gizmohub.service.BTCommandListener;
import com.justonesoft.netbot.framework.android.gizmohub.service.ServiceGateway;
import com.justonesoft.netbot.framework.android.gizmohub.service.UICommandListener;
import com.justonesoft.netbot.framework.android.gizmohub.service.streaming.CameraStreamer;
import com.justonesoft.netbot.util.StatusUpdateHandler;

import java.io.IOException;

public class IOTAccessPointActivity extends ActionBarActivity {

    /**
     *
     * can be idle(sleeping) or listening for command
     */
    private byte state;

    private boolean isBluetoothConnected = false;

    private final static String BLUETOOTH_DEVICE_NAME = "HC-05";
    private final static String HUB_SERVER_NAME = "service.gizmo-hub.com";
    private final static int HUB_SERVER_PORT = 9999;

    private TextView status_text_view;

    ServiceGateway gateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LIFE_FLOW", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iotaccess_point);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_stream);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // aim for 1 fps
                    gateway.startStreaming();
                } else {
                    gateway.stopStreaming();
                }
            }
        });

        status_text_view = (TextView) findViewById(R.id.status_text);
        status_text_view.setMovementMethod(new ScrollingMovementMethod());

        // now we try to connect to server
        status_text_view.setText("Trying to connect...\n");

        EditText serverAddressEdit = (EditText) findViewById(R.id.server_address);
        serverAddressEdit.setText(HUB_SERVER_NAME);

        final Handler handler = new Handler();
        try {
            connectToGateway(HUB_SERVER_NAME, HUB_SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            status_text_view.setText("Could not connect!\n");
        }

//        status_text_view.post(new Runnable() {
//            public void run() {
//                status_text_view.scrollTo(0, status_text_view.getBottom());
//            }
//        });
    }

    private void connectToGateway(String serverAddress, int serverPort) throws IOException {

        StatusUpdateHandler statusTextUpdater = new StatusUpdateHandler(status_text_view);

        if (gateway == null) {
            CameraStreamer cs = new CameraStreamer((FrameLayout) findViewById(R.id.camera_preview));

            prepareAndConnectBluetooth(BLUETOOTH_DEVICE_NAME);


            gateway = new ServiceGateway(serverAddress, serverPort);
            gateway.registerCommandListener(BTController.getInstance());
            gateway.registerCommandListener(new UICommandListener(statusTextUpdater));
            gateway.registerCommandListener(cs);
            gateway.registerStreamer(cs);
        }

        if (gateway != null && !gateway.isConnected()) {
            gateway.connect();
        }
    }

    private void prepareAndConnectBluetooth(String bluetoothDeviceName) {
        BluetoothDevice btDevice = BTController.getInstance().getPairedBTDeviceByName(bluetoothDeviceName);
        if (btDevice != null) {
            BTController.getInstance().connectWithBTDevice(btDevice);
        }
    }

    public void reconnect(View view) {
        if (gateway != null && gateway.isConnected()) {
            gateway.disconnect();
            gateway = null; // needs to be null for connectToGateway method
        }
        // now we try to connect to server
        status_text_view.setText("Trying to connect...\n");

        EditText serverAddressEdit = (EditText) findViewById(R.id.server_address);
        String serverAddress = serverAddressEdit.getText().toString();
        try {
            connectToGateway(serverAddress, HUB_SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            status_text_view.setText("Could not connect!\n");
        }
    }

    private String createDummyText() {
        String s = "";

        for (int i=0; i<7; i++) {

            s += "Line " + i + "\n";
        }

        return s;
    }

    private void disconnectAndReleaseCamera() {

    }

    private void disconnectFromSocket() {
        if (gateway != null) {
            gateway.disconnect();
        }
    }

    private void disconnectFromBluetooth() {
        isBluetoothConnected = false;
        BTController.getInstance().disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LIFE_FLOW", "onDestroy");

        disconnectFromSocket();
        disconnectFromBluetooth();
        disconnectAndReleaseCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LIFE_FLOW", "onStop");

//        disconnectFromSocket();
//        disconnectFromBluetooth();
//        disconnectAndReleaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LIFE_FLOW", "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("LIFE_FLOW", "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LIFE_FLOW", "onPause");
    }
}
