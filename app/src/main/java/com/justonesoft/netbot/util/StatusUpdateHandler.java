package com.justonesoft.netbot.util;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by bmunteanu on 4/1/2016.
 */
public class StatusUpdateHandler extends Handler {

    WeakReference<TextView> wrStatusTextView;

    public StatusUpdateHandler(TextView statusTextView) {
        wrStatusTextView = new WeakReference<TextView>(statusTextView);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        TextView statusTextView = wrStatusTextView.get();
        if (statusTextView != null) {
            TextViewUtil.prefixWithText(statusTextView, (String )msg.obj, true);
        }
    }
}
