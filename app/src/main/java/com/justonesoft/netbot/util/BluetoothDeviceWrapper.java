package com.justonesoft.netbot.util;

import android.bluetooth.BluetoothDevice;

/**
 * This is a wrapper around BluetoothDevice to be used with an ArrayAdapter so that the toString() method return the device name.
 * This is needed for the ArrayAdapter, because it calls the toString method for the text to be displayed.
 * <br /> <br />
 *
 * Created by bmunteanu on 4/28/2015.
 */
public class BluetoothDeviceWrapper {
    private BluetoothDevice bluetoothDevice;

    public BluetoothDeviceWrapper(final BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    /**
     * Return the name of the internal BluetoothDevice
     */
    public String toString() {
        return this.bluetoothDevice.getName();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
}
