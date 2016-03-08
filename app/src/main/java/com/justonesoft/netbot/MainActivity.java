package com.justonesoft.netbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.justonesoft.netbot.bt.BTController;
import com.justonesoft.netbot.util.BluetoothDeviceWrapper;
import com.justonesoft.netbot.util.TextViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The entry point in the application as configured in the AndroidManifest.xml
 *
 */
public class MainActivity extends Activity {
    private static final int BT_ENABLE_CODE = 1;
    public static final String BT_DEVICE_MAC = "com.justonesoft.netbot.BT_DEVICE_MAC";

    private BTController btController = BTController.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Will initialize the list view with the list of paired devices.
     */
    @Override
    protected void onResume() {
        super.onResume();

        TextView statusText = (TextView) findViewById(R.id.textStatus);
    }

    /**
     * This method gets called after any activity started by this activity returns.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // called if the "Enable Bluetooth" activity was started.
    }

    /**
     * Will open the activity from where you can chose the BT device to be used to communicate with.
     */
    private void openSelectBTDeviceActivity() {
        Intent navigateActivityIntent = new Intent(this, SelectBTActivity.class);
        startActivity(navigateActivityIntent);
    }

    private void openCameraStreamingActivity() {
        Intent cameraStreamingActivity = new Intent(this, CameraStreamingActivity.class);
        startActivity(cameraStreamingActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Button method. Will be executed when button "remote_control_button" is pressed.
     * @param view
     */
    public void startRemoteControl(View view) {
        openSelectBTDeviceActivity();
    }

    public void startCameraStreaming(View view) {
        openCameraStreamingActivity();
    }

    public void startIOTAccessPoint(View view) {
    }
}
