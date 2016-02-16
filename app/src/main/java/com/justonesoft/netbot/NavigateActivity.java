package com.justonesoft.netbot;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.justonesoft.netbot.bt.BTController;
import com.justonesoft.netbot.util.Commands;
import com.justonesoft.netbot.util.TextViewUtil;

import java.util.Set;

/**
 * This class is where user can send commands to the controlled bot over Bluetooth. <br />
 * Here also happens the actual Bluetooth connection with the controlled bot.
 */
public class NavigateActivity extends ActionBarActivity implements View.OnTouchListener {

    private BluetoothDevice bluetoothDevice;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        Intent intent = getIntent();

        if (intent != null) {
            // get the device address; this has been put here by the MainActivity activity
            String deviceAddress = intent.getStringExtra(MainActivity.BT_DEVICE_MAC);

            // look for the actual BluetoothDevice
            Set<BluetoothDevice> pairedDevices = BTController.getInstance().getPairedDevices();

            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getAddress().equals(deviceAddress)) {
                    this.bluetoothDevice = bt;
                }
            }
        }

        // register the click and longClick events for buttons
        addTouchClickEventsToButtons();

        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                TextView statusText = (TextView) findViewById(R.id.nav_status_text);
                TextViewUtil.prefixWithText(statusText, String.valueOf(msg.what), false);
            }
        };
    }

    private void addTouchClickEventsToButtons() {
        ((Button) findViewById(R.id.up_button)).setOnTouchListener(this);

        ((Button) findViewById(R.id.down_button)).setOnTouchListener(this);

        ((Button) findViewById(R.id.left_button)).setOnTouchListener(this);

        ((Button) findViewById(R.id.right_button)).setOnTouchListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView statusText = (TextView) findViewById(R.id.nav_status_text);
        if (this.bluetoothDevice == null) {
            TextViewUtil.prefixWithText(statusText, getText(R.string.err_inval_bt_device), true);
        } else {
            // perform the Bluetooth connection with the bot
            TextViewUtil.prefixWithText(statusText, getText(R.string.connecting_to_device) + this.bluetoothDevice.getName(), true);
            BTController.getInstance().connectWithBTDevice(this.bluetoothDevice, statusText);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTController.getInstance().disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigate, menu);
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
     * When used presses anywhere on the screen this method will be called.
     * Here it is checked the "press" action that can be" down or up or something else. I'm only interested in the "down" and "up" events.
     * After the event has been identified, I check to see on which component the event happened: a button or somewhere else.
     *
     * @param v The component on which the event happened, like a Button.
     * @param event The actual event, like "down" or "up" when a finger touches or releases the screen.
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TextView statusText = (TextView) findViewById(R.id.nav_status_text);

        switch (event.getAction()) {
            // identify the event
            case MotionEvent.ACTION_DOWN:
                // start sending commands
                byte commandToSend = Commands.STOP.getWhatToSend();
                // identify the component
                switch (v.getId()) {
                    case R.id.up_button:
                        commandToSend = Commands.MOVE_FORWARD.getWhatToSend();
                        break;
                    case R.id.down_button:
                        commandToSend = Commands.MOVE_BACKWARDS.getWhatToSend();
                        break;
                    case R.id.left_button:
                        commandToSend = Commands.TURN_LEFT.getWhatToSend();
                        break;
                    case R.id.right_button:
                        commandToSend = Commands.TURN_RIGHT.getWhatToSend();
                        break;
                }
                BTController.getInstance().sendCommand(commandToSend);
                handler.obtainMessage(commandToSend).sendToTarget();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                commandToSend = Commands.STOP.getWhatToSend();
                BTController.getInstance().sendCommand(commandToSend);
                handler.obtainMessage(commandToSend).sendToTarget();
                break;
        }
        return false;
    }
}
