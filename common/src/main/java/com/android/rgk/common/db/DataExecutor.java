package com.android.rgk.common.db;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DataExecutor {
    private static DataExecutor mInstance;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private DataExecutor() {
    }

    public void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    public <T> Future<T> execute(Callable<T> callable) {
        return mExecutor.submit(callable);
    }

    private void shutDown() {
        mExecutor.shutdownNow();
        mExecutor = null;
    }

    public static synchronized DataExecutor getInstance() {
        if (mInstance == null) {
            mInstance = new DataExecutor();
        }
        return mInstance;
    }

    public static void release() {
        if (mInstance != null) {
            mInstance.shutDown();
            mInstance = null;
        }
    }
}
