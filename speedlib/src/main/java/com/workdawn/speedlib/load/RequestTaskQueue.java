package com.workdawn.speedlib.load;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.workdawn.speedlib.Status;
import com.workdawn.speedlib.core.Speed;
import com.workdawn.speedlib.core.SpeedOption;
import com.workdawn.speedlib.db.IDatabase;
import com.workdawn.speedlib.executor.Dispatcher;
import com.workdawn.speedlib.executor.ExecutorManager;
import com.workdawn.speedlib.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public class RequestTaskQueue {

    private static volatile RequestTaskQueue sRequestTaskQueue;
    //all tasks
    private final ConcurrentHashMap<String, RequestTask> tasks = new ConcurrentHashMap<>();
    //tasks uniqueIds
    private final ConcurrentHashMap<String, String> uniqueKeys = new ConcurrentHashMap<>();
    //running
    private final PriorityBlockingQueue<RequestTask> runningTaskQueue = new PriorityBlockingQueue<>();
    //wait to run
    private final PriorityBlockingQueue<RequestTask> resumeTaskQueue = new PriorityBlockingQueue<>();
    //wait to resume
    private final PriorityBlockingQueue<RequestTask> pauseTaskQueue = new PriorityBlockingQueue<>();

    private NetworkListenerBroadcastReceiver networkListenerBroadcastReceiver = null;
    private SpeedOption mSpeedOption;

    private List<Future> futures = new ArrayList<>();

    private IDatabase database;

    private AtomicInteger currentRunningTaskCount = new AtomicInteger();

    volatile static boolean DISPATCHER_INIT = false;

    private Dispatcher dispatcher;

    private RequestTaskQueue(Context context, SpeedOption speedOption){
        mSpeedOption = speedOption;
        if(mSpeedOption.autoMaxAllowRunningTaskNum){
            networkListenerBroadcastReceiver = new NetworkListenerBroadcastReceiver(this, context);
            networkListenerBroadcastReceiver.register();
        }
    }

    public static RequestTaskQueue newInstance(Context context, SpeedOption speedOption){
        if(sRequestTaskQueue == null){
            synchronized (RequestTaskQueue.class) {
                if(sRequestTaskQueue == null){
                    sRequestTaskQueue = new RequestTaskQueue(context, speedOption);
                }
            }
        }
        return sRequestTaskQueue;
    }

    public void putUniqueKey(String url, String uniqueId){
        uniqueKeys.put(url, uniqueId);
    }

    void incrementRunningTaskCount(){
        currentRunningTaskCount.incrementAndGet();
    }

    void decrementRunningTaskCount(){
        currentRunningTaskCount.decrementAndGet();
    }

    void setDispatcher(Dispatcher dispatcher){
        this.dispatcher = dispatcher;
    }

    public String getUniqueKey(String url){
        return uniqueKeys.get(url);
    }

    public Status getStatus(String uniqueId){
        RequestTask requestTask = tasks.get(uniqueId);
        return requestTask == null ? Status.INIT : requestTask.getStatus();
    }


    public RequestTask getRequestTask(String uniqueId){
        return tasks.get(uniqueId);
    }

    public RequestTask takeRequestTask() throws InterruptedException{
        return runningTaskQueue.take();
    }

    void clearCompleteTaskFromMap(RequestTask requestTask){
        tasks.remove(requestTask.getUniqueId(), requestTask);
        uniqueKeys.remove(requestTask.getUrl(), requestTask.getUniqueId());
        pollRequestTaskToRunningQueue();
    }

    void pollRequestTaskToRunningQueue(){
        if(resumeTaskQueue.size() > 0){
            if(getCurrentRunningTaskNum() < mSpeedOption.maxAllowRunningTaskNum) {
                runningTaskQueue.put(resumeTaskQueue.poll());
            }
        }else {
            if(canExit() && mSpeedOption.autoExit){
                shutDown();
            }
        }
    }

    private boolean canExit(){
        return pauseTaskQueue.size() == 0
                && getCurrentRunningTaskNum() == 0;
    }

    void addTaskToPauseQueue(RequestTask requestTask){
        pauseTaskQueue.put(requestTask);
        pollRequestTaskToRunningQueue();
    }

    void addPauseTaskToResumeQueue(){
        if(pauseTaskQueue.size() > 0){
            RequestTask task = pauseTaskQueue.poll();
            addTaskToResumeQueue(task);
            pollRequestTaskToRunningQueue();
        }
    }

    void addTaskToResumeQueue(RequestTask task){
        resumeTaskQueue.put(task);
        pollRequestTaskToRunningQueue();
    }

    private int getCurrentRunningTaskNum() {
        return runningTaskQueue.size() + currentRunningTaskCount.get();
    }

    IDatabase getDatabase(){
        if(database == null){
            synchronized (RequestTaskQueue.class) {
                if(database == null){
                    database = Speed.getDatabase();
                }
            }
        }
        return database;
    }

    void addFuture(Future future){
        futures.add(future);
    }

    void addTaskToResumeOrRunningQueue(RequestTask requestTask) {
        tasks.put(requestTask.getUniqueId(), requestTask);
        if(getCurrentRunningTaskNum() < mSpeedOption.maxAllowRunningTaskNum){
            runningTaskQueue.put(requestTask);
        } else {
            requestTask.setStatus(Status.RESUME);
            resumeTaskQueue.put(requestTask);
        }
    }

    void removeRequestTask(RequestTask requestTask){
        if(runningTaskQueue.contains(requestTask)){
            runningTaskQueue.remove(requestTask);
        } else if(resumeTaskQueue.contains(requestTask)){
            resumeTaskQueue.remove(requestTask);
        } else if(pauseTaskQueue.contains(requestTask)){
            pauseTaskQueue.remove(requestTask);
        }
        if(tasks.contains(requestTask)){
            tasks.remove(requestTask.getUniqueId(), requestTask);
        }
        if(uniqueKeys.containsKey(requestTask.getUrl())){
            uniqueKeys.remove(requestTask.getUrl(), requestTask.getUniqueId());
        }

        if(canExit() && mSpeedOption.autoExit){
            shutDown();
        }

        pollRequestTaskToRunningQueue();
    }

    public void start(RequestTask requestTask){
        requestTask.start();
    }

    public void reStart(RequestTask requestTask){
        if(getCurrentRunningTaskNum() < mSpeedOption.maxAllowRunningTaskNum){
            runningTaskQueue.put(requestTask);
        } else {
            requestTask.reStart();
        }
    }

    public void pause(RequestTask requestTask){
        requestTask.pause();
    }

    public void resume(RequestTask requestTask){
        requestTask.resume();
    }

    public void cancel(RequestTask requestTask){
        requestTask.cancel();
    }

    public void cancel(List<RequestTask> tasks){
        for (RequestTask requestTask : tasks) {
            requestTask.cancel();
        }
    }

    public void cancelAll(){
        ExecutorManager.newInstance().getBackgroundExecutor().submit(new Runnable() {
            @Override
            public void run() {
                for (RequestTask requestTask : runningTaskQueue) {
                    requestTask.setStatus(Status.CANCEL);
                }
                shutDown();
            }
        });
    }

    void dispatchNetworkChange(NetworkInfo info){
        Utils.adjustMaxRunningTaskCount(info, mSpeedOption);
    }

    private void shutDown(){
        if(networkListenerBroadcastReceiver != null){
            networkListenerBroadcastReceiver.unregister();
        }
        for (Future future : futures) {
            future.cancel(true);
        }
        ExecutorManager.newInstance().getBackgroundExecutor().shutdown();
        tasks.clear();
        uniqueKeys.clear();
        runningTaskQueue.clear();
        resumeTaskQueue.clear();
        pauseTaskQueue.clear();
        if(database != null){
            database.close();
            database = null;
        }
        dispatcher.setExit(true);
        sRequestTaskQueue = null;
        mSpeedOption = null;
        DISPATCHER_INIT = false;
    }

    static class NetworkListenerBroadcastReceiver extends BroadcastReceiver {

        RequestTaskQueue taskQueue;
        Context mContext;
        NetworkListenerBroadcastReceiver(RequestTaskQueue taskQueue, Context context){
            this.taskQueue = taskQueue;
            mContext = context;
        }

        void register() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(CONNECTIVITY_ACTION);
            mContext.registerReceiver(this, filter);
        }

        void unregister(){
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null){
                return;
            }
            String action = intent.getAction();
            if (CONNECTIVITY_ACTION.equals(action)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                taskQueue.dispatchNetworkChange(cm.getActiveNetworkInfo());
            }
        }
    }
}
