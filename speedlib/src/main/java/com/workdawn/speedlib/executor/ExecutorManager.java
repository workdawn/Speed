package com.workdawn.speedlib.executor;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2018/4/26.
 * @author workdawn
 */

public class ExecutorManager {

    private static ExecutorManager executorManager;

    private ExecutorManager(){}

    public static ExecutorManager newInstance(){
        if(executorManager == null){
            synchronized (ExecutorManager.class) {
                if(executorManager == null){
                    executorManager = new ExecutorManager();
                }
            }
        }
        return executorManager;
    }

    private ExecutorService backgroundExecutor;
    private Executor callbackExecutor;

    public ExecutorService getBackgroundExecutor(){
        if(backgroundExecutor == null){
            synchronized (ExecutorManager.class) {
                if(backgroundExecutor == null){
                    backgroundExecutor = Executors.newCachedThreadPool();
                }
            }
        }
        return backgroundExecutor;
    }

    public Executor getCallbackExecutor(){
        if(callbackExecutor == null){
            synchronized (ExecutorManager.class) {
                if(callbackExecutor == null){
                    callbackExecutor = new MainThreadExecutor();
                }
            }
        }
        return callbackExecutor;
    }

    static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public void execute(@NonNull  Runnable r) {
            handler.post(r);
        }
    }
}
