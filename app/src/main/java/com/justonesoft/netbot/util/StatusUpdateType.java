package com.justonesoft.netbot.util;

import com.justonesoft.netbot.R;

/**
 * Created by bmunteanu on 2/17/2016.
 */
public enum StatusUpdateType {

    BT_STATUS_CONNECTED(R.string.bluetooth_connected),
    BT_STATUS_COMMUNICATION_READY(R.string.bluetooth_communication_ready),
    BT_STATUS_ERROR_CONNECT(R.string.err_could_not_connect_bluetooth),
    BT_STATUS_ERROR_NO_SOCKET(R.string.err_no_bluetooth_socket),
    BT_STATUS_ERROR_COMMUNICATION(R.string.err_no_communication);

    private int uiResourceId;

    private StatusUpdateType() {

    }

    private StatusUpdateType(int uiResourceId) {
        this.uiResourceId = uiResourceId;
    }

    public final int getUiResourceId() {
        return uiResourceId;
    }
}
