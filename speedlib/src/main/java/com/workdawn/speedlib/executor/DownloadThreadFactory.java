package com.workdawn.speedlib.executor;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * Created on 2018/5/26.
 * @author workdawn
 */
class DownloadThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(@NonNull Runnable r) {

        return new ExecutorThread(r);
    }
}
