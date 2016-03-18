package com.justonesoft.netbot.util;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bmunteanu on 2/17/2016.
 */
public class StatusTextUpdaterManager {

    private static AtomicInteger idGenerator = new AtomicInteger();

    private static HashMap<Integer, StatusTextUpdater> pool = new HashMap<Integer, StatusTextUpdater>();

    public static void registerTextUpdater(int textUpdaterId, StatusTextUpdater textUpdater) {
        if (textUpdater == null) return;
        pool.put(textUpdaterId, textUpdater);
    }

    public static void updateStatusText(int textUpdaterId, int statusType, Object payload) {
        if (pool.get(textUpdaterId) == null) return;
        pool.get(textUpdaterId).updateStatusText(statusType, payload);
    }

    public static void updateStatusText(int textUpdaterId, String statusText) {
        if (pool.get(textUpdaterId) == null) return;
        pool.get(textUpdaterId).updateStatusText(statusText);
    }

    public static void updateStatusText(int textUpdaterId, int statusType) {
        if (pool.get(textUpdaterId) == null) return;
        pool.get(textUpdaterId).updateStatusText(statusType);
    }

    public static int nextId() {
        return idGenerator.incrementAndGet();
    }
}
