package com.workdawn.speedlib.executor;

import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.load.RequestTaskQueue;
import com.workdawn.speedlib.utils.LogUtils;

/**
 * Created on 2018/4/27.
 * @author workdawn
 */
public class Dispatcher implements Runnable{

    private RequestTaskQueue requestTaskQueue;
    private boolean exit = false;

    public void setExit(boolean exit){
        this.exit = exit;
    }

    public Dispatcher(RequestTaskQueue requestTaskQueue){
        this.requestTaskQueue = requestTaskQueue;
    }

    @Override
    public void run() {
        while (!exit) {
            try {
                RequestTask requestTask = requestTaskQueue.takeRequestTask();
                requestTask.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
                exit = true;
                LogUtils.i("Dispatcher is exit");
            }
        }
    }
}
