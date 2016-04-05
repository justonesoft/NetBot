package com.justonesoft.netbot.framework.android.gizmohub.service;

/**
 * Created by bmunteanu on 3/31/2016.
 */
public interface CommandListener {
    /**
     *  should return a Command
     */
    public void listenForCommand();

    /**
     * Waits for commands and executes them
     */
    public void listenAndExecuteCommands();
}
