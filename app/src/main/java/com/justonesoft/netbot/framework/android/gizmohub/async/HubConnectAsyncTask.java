package com.justonesoft.netbot.framework.android.gizmohub.async;

import android.os.AsyncTask;

import com.justonesoft.netbot.framework.android.gizmohub.service.Hub;

import java.util.concurrent.Future;

/**
 * Created by bmunteanu on 4/1/2016.
 */
public class HubConnectAsyncTask extends AsyncTask<Hub, Void, Hub> {

    public void connectHub(Hub hub) {
        execute(hub);
    }

    @Override
    protected Hub doInBackground(Hub... params) {
        Hub hub = params[0];
        hub.connect();
        return hub;
    }

    @Override
    protected void onPostExecute(Hub hub) {
        super.onPostExecute(hub);
        doAfterConnecting(hub.isConnected());
    }

    /**
     * Will be called on the thread that instantiates this object after the connection operation has finished.
     * It will be called regardles if connection is successfull or not.
     *
     * @param connected true if connection was successful, false otherwise
     */
    public void doAfterConnecting(boolean connected) {
        // default does nothing
    }
}
