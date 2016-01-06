package com.justonesoft.netbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
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


public class MainActivity extends Activity {
    private static final int BT_ENABLE_CODE = 1;
    public static final String BT_DEVICE_MAC = "com.justonesoft.netbot.BT_DEVICE_MAC";

    private BTController btController = BTController.getInstance();
    private DialogFragment dialogFragment = new DialogFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView pairedDevicesList = (ListView) findViewById(R.id.listPairedDevices);
        pairedDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDeviceWrapper btDev = (BluetoothDeviceWrapper) parent.getAdapter().getItem(position);

                if (btDev != null) {
                    connectAndOpenDriveActivity(btDev.getBluetoothDevice());
                } else {
                    TextView statusText = (TextView) findViewById(R.id.textStatus);
                    TextViewUtil.prefixWithText(statusText, getText(R.string.err_inval_bt_device), true);
                }
            }
        });
        // check if Bluetooth is enabled
        if (!btController.deviceHasBluetooth()) {
            // display an alert dialog
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

    private void showPairedDevicesList(TextView statusText) {
        // create a list with all paired devices
        Set<BluetoothDevice> pairedDevices = btController.getPairedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            TextViewUtil.prefixWithText(statusText, getText(R.string.err_no_paired), true);
            return;
        }

        List<BluetoothDeviceWrapper> devicesForList = new ArrayList<BluetoothDeviceWrapper>(pairedDevices.size());
        for (BluetoothDevice btDevice : pairedDevices) {
            devicesForList.add(new BluetoothDeviceWrapper(btDevice));
        }
        ArrayAdapter<BluetoothDeviceWrapper> pairedDevicesAdapter = new ArrayAdapter<BluetoothDeviceWrapper>(
                this, android.R.layout.simple_list_item_1, devicesForList);
        ListView pairedDevicesList = (ListView) findViewById(R.id.listPairedDevices);
        pairedDevicesList.setAdapter(pairedDevicesAdapter);

    }

    private void connectAndOpenDriveActivity(BluetoothDevice deviceToConnect) {
        Intent navigateActivityIntent = new Intent(this, NavigateActivity.class);
        navigateActivityIntent.putExtra(BT_DEVICE_MAC,deviceToConnect.getAddress());
        startActivity(navigateActivityIntent);
    }
}
