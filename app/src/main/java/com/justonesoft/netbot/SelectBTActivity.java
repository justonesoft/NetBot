package com.justonesoft.netbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.justonesoft.netbot.NavigateActivity;
import com.justonesoft.netbot.R;
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
public class SelectBTActivity extends ActionBarActivity {
    private static final int BT_ENABLE_CODE = 1;
    public static final String BT_DEVICE_MAC = "com.justonesoft.netbot.BT_DEVICE_MAC";

    private BTController btController = BTController.getInstance();
    private DialogFragment dialogFragment = new DialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // this list has been initialized with records in onResume() method.
        ListView pairedDevicesList = (ListView) findViewById(R.id.listPairedDevices);

        // Here is the code to be executed when an Item on the paired devices list is selected.
        pairedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPairedDeviceSelected((BluetoothDeviceWrapper) parent.getAdapter().getItem(position));
            }
        });

        // check if Bluetooth is enabled
        if (!btController.deviceHasBluetooth()) {
            // display an alert dialog because the device has no bluetooth. (like and old phone)
            new AlertDialog.Builder(this)
                    .setTitle(R.string.no_bluetooth)
                    .create().show();
        } else {
            if (!btController.isEnabled()) {
                // create an intent to ask user to enable Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BT_ENABLE_CODE);
            }
        }
    }

    /**
     * Open the NavigateActivity using the device that user has selected from the paired devices list.
     * @param item
     */
    private void onPairedDeviceSelected(BluetoothDeviceWrapper item) {
        BluetoothDeviceWrapper btDev = item;

        if (btDev != null) {
            openNavigateActivity(btDev.getBluetoothDevice());
        } else {
            TextView statusText = (TextView) findViewById(R.id.textStatus);
            TextViewUtil.prefixWithText(statusText, getText(R.string.err_inval_bt_device), true);
        }
    }

    /**
     * Will initialize the list view with the list of paired devices.
     */
    @Override
    protected void onResume() {
        super.onResume();

        TextView statusText = (TextView) findViewById(R.id.textStatus);

        // check if BT is enabled
        if (btController.isEnabled()) {
            // is enabled, show the list of paired devices
           TextViewUtil.prefixWithText(statusText, getText(R.string.status_bt_enalbed), true);
           showPairedDevicesList(statusText);
       } else {
            TextViewUtil.prefixWithText(statusText, getText(R.string.err_not_connected), true);
        }
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

        if (requestCode == BT_ENABLE_CODE) {
            // we need to check if user accepted to enable bluetooth
            if (resultCode != RESULT_OK) {
                // user chose not to enable
            }
        }
    }

    /**
     * Displays a list with all the Bluetooth devices that this devices is paired. <br />
     *
     * If no devices are paired, an error string will be displayed.
     *
     * @param statusText Used to append messages in the "message" area.
     */
    private void showPairedDevicesList(TextView statusText) {
        // create a list with all paired devices
        Set<BluetoothDevice> pairedDevices = btController.getPairedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            TextViewUtil.prefixWithText(statusText, getText(R.string.err_no_paired), true);
            return;
        }

        // wrap each paired device BluetoothDevice object into a BluetoothDeviceWrapper object.
        List<BluetoothDeviceWrapper> devicesForList = new ArrayList<BluetoothDeviceWrapper>(pairedDevices.size());
        for (BluetoothDevice btDevice : pairedDevices) {
            devicesForList.add(new BluetoothDeviceWrapper(btDevice));
        }

        // create and ArrayAdapter with the list of paired devices
        ArrayAdapter<BluetoothDeviceWrapper> pairedDevicesAdapter = new ArrayAdapter<BluetoothDeviceWrapper>(
                this, android.R.layout.simple_list_item_1, devicesForList);

        // get the view id and add the adapter
        ListView pairedDevicesList = (ListView) findViewById(R.id.listPairedDevices);
        pairedDevicesList.setAdapter(pairedDevicesAdapter);

    }

    /**
     * Will open the NavigateActivity and set the address of the selected device as payload for that activity.
     * The NavigateActivity will use that address to actually connect with the Bluetooth device.
     *
     * @param deviceToConnect
     */
    private void openNavigateActivity(BluetoothDevice deviceToConnect) {
        Intent navigateActivityIntent = new Intent(this, NavigateActivity.class);
        navigateActivityIntent.putExtra(BT_DEVICE_MAC, deviceToConnect.getAddress());
        startActivity(navigateActivityIntent);
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
}
