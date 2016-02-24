package com.justonesoft.netbot.util;

/**
 * Created by bmunteanu on 2/17/2016.
 */
public interface StatusTextUpdater {

    public void updateStatusText(int statusType, Object payload);

    public void updateStatusText(int statusType);

    public void updateStatusText(String statusText);

}
