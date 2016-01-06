package com.justonesoft.netbot.util;

import android.bluetooth.BluetoothDevice;

/**
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
