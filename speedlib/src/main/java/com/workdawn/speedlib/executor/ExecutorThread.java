package com.workdawn.speedlib.executor;

import com.workdawn.speedlib.utils.LogUtils;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created on 2018/5/26.
 * @author workdawn
 */
class ExecutorThread extends Thread {
    public ExecutorThread(Runnable r) {
        super(r);
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
        LogUtils.i("当前执行的线程名：" + Thread.currentThread().getName());
        super.run();
    }
}
