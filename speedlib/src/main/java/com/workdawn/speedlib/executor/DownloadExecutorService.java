package com.workdawn.speedlib.executor;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2018/5/26.
 *
 * @author workdawn
 */
public class DownloadExecutorService extends ThreadPoolExecutor {

    public DownloadExecutorService(int nThreads) {
        super(0, Integer.MAX_VALUE, 60L,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                new DownloadThreadFactory());
    }

}

