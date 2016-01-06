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
import com.justonesoft.netbot.util.TextViewUtil;

import java.util.Set;


public class NavigateActivity extends ActionBarActivity implements View.OnTouchListener {

    private BluetoothDevice bluetoothDevice;
    private CommandSender commandSender;
    private Handler handler;

    private static final byte UP = 1;
    private static final byte DOWN = 2;
    private static final byte LEFT = 3;
    private static final byte RIGHT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        Intent intent = getIntent();

        if (intent != null) {
            // get the device address
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        TextView statusText = (TextView) findViewById(R.id.nav_status_text);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // start sending commands
                if (commandSender != null) {
                    TextViewUtil.prefixWithText(statusText, getText(R.string.err_already_sending_command), true);
                    return false;
                }
                byte command = 0;
                switch (v.getId()) {
                    case R.id.up_button:
                        command = UP;
                        break;
                    case R.id.down_button:
                        command = DOWN;
                        break;
                    case R.id.left_button:
                        command = LEFT;
                        break;
                    case R.id.right_button:
                        command = RIGHT;
                        break;
                }
                if (command != 0) {
                    commandSender = new CommandSender(command);
                    commandSender.start();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                if (commandSender != null) {
                    commandSender.stopSendingCommand();
                    commandSender = null;
                }
                break;
        }
        return false;
    }

    class CommandSender extends Thread {
        private byte command;
        private boolean stop = false;

        public CommandSender(byte command) {
            this.command = command;
        }

        public void stopSendingCommand() {
            stop = true;
        }

        @Override
        public void run() {
            while (!stop) {
                BTController.getInstance().sendCommand(command);
                handler.obtainMessage(command).sendToTarget();
                try {
                    // Just a short pause in case the button is kept pressed. Worth trying without this pause at all.
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
