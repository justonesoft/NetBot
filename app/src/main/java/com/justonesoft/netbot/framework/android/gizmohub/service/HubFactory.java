package com.justonesoft.netbot.framework.android.gizmohub.service;

/**
 * Created by bmunteanu on 4/1/2016.
 */
public class HubFactory {
    public static Hub getHub(String serverAddress, int port) {
        return new SocketHub(serverAddress, port);
        // maybe try to connect here so that the hub is ready for accepting commands?
    }
}
