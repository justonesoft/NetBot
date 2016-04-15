package com.justonesoft.netbot;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.justonesoft.netbot.bt.BTController;
import com.justonesoft.netbot.camera.CameraManager;
import com.justonesoft.netbot.camera.CameraPreview;
import com.justonesoft.netbot.util.Commands;
import com.justonesoft.netbot.util.StatusTextUpdater;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;
import com.justonesoft.netbot.util.StatusUpdateType;
import com.justonesoft.netbot.util.TextViewUtil;

import java.util.Set;

/**
 * This class is where user can send commands to the controlled bot over Bluetooth. <br />
 * Here also happens the actual Bluetooth connection with the controlled bot.
 */
public class NavigateActivity extends ActionBarActivity implements View.OnTouchListener, StatusTextUpdater {

    public final static int COMMAND_SENT_ID = 100;
    public final static int TEXT = 101;
    public final static int TEXT_UPDATER_ID = StatusTextUpdaterManager.nextId();

    private BluetoothDevice bluetoothDevice;
    private Handler handler;

    @Override
    public void updateStatusText(int statusType, Object payload) {
        this.handler.obtainMessage(statusType, payload).sendToTarget();
    }

    @Override
    public void updateStatusText(int statusType) {
        this.handler.obtainMessage(statusType).sendToTarget();
    }

    @Override
    public void updateStatusText(String statusText) {
        this.handler.obtainMessage(TEXT, statusText).sendToTarget();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);

        Intent intent = getIntent();

        if (intent != null) {
            // get the device address; this has been put here by the MainActivity activity
            String deviceAddress = intent.getStringExtra(MainActivity.BT_DEVICE_MAC);
            this.bluetoothDevice = BTController.getInstance().getPairedBTDeviceByAddress(deviceAddress);
        }

        // register the click and longClick events for buttons
        addTouchClickEventsToButtons();

        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                TextView statusTextView = (TextView) findViewById(R.id.nav_status_text);

                if (statusTextView != null) {
                    // update the UI based on the status
                    switch (msg.what) {
                        case BTController.BT_STATUS:
                            TextViewUtil.prefixWithText(statusTextView, getText(((StatusUpdateType)msg.obj).getUiResourceId()), true);
                            break;
                        case COMMAND_SENT_ID:
                            TextViewUtil.prefixWithText(statusTextView, msg.obj.toString(), false);
                            break;
                        case TEXT:
                            TextViewUtil.prefixWithText(statusTextView, msg.obj.toString(), true);
                            break;
                        default:
                            TextViewUtil.prefixWithText(statusTextView, msg.obj.toString(), true);
                    }
                }
            }
        };

        StatusTextUpdaterManager.registerTextUpdater(TEXT_UPDATER_ID, this);

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
            BTController.getInstance().connectWithBTDevice(this.bluetoothDevice);
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
                // connect sending commands
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
                updateStatusText(COMMAND_SENT_ID, Integer.valueOf(commandToSend));

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                commandToSend = Commands.STOP.getWhatToSend();
                BTController.getInstance().sendCommand(commandToSend);
                updateStatusText(COMMAND_SENT_ID, Integer.valueOf(commandToSend));
                break;
        }
        return false;
    }
}
