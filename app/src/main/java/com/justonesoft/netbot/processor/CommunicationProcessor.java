package com.justonesoft.netbot.processor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.justonesoft.netbot.NavigateActivity;
import com.justonesoft.netbot.R;
import com.justonesoft.netbot.util.StatusTextUpdaterManager;
import com.justonesoft.netbot.util.TextViewUtil;

/**
 * Created by bmunteanu on 2/17/2016.
 */
public class CommunicationProcessor {

    public CommunicationProcessor() {
    }

    public void process(byte[] data) {
        // we need to see if there is a command or just text
        // a command starts with # and the next byte is the actual command
        String receivedData = new String(data);

        StatusTextUpdaterManager.updateStatusText(NavigateActivity.TEXT_UPDATER_ID, receivedData);
    }
}
