package com.workdawn.speedlib.load;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseArray;

import com.workdawn.speedlib.Status;
import com.workdawn.speedlib.callback.ITaskGroupDownloadProgressCallback;
import com.workdawn.speedlib.callback.ITaskGroupDownloadResultCallback;
import com.workdawn.speedlib.core.Speed;
import com.workdawn.speedlib.core.SpeedOption;
import com.workdawn.speedlib.db.IDatabase;
import com.workdawn.speedlib.executor.Dispatcher;
import com.workdawn.speedlib.executor.ExecutorManager;
import com.workdawn.speedlib.utils.ByteArrayPool;
import com.workdawn.speedlib.utils.Utils;

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
    //all task
    private final ConcurrentHashMap<String, RequestTask> tasks = new ConcurrentHashMap<>();
    //task uniqueId
    private final ConcurrentHashMap<String, String> uniqueKeys = new ConcurrentHashMap<>();
    //running
    private final PriorityBlockingQueue<RequestTask> runningTaskQueue = new PriorityBlockingQueue<>();
    //wait to run
    private final PriorityBlockingQueue<RequestTask> resumeTaskQueue = new PriorityBlockingQueue<>();
    //wait to resume
    private final PriorityBlockingQueue<RequestTask> pauseTaskQueue = new PriorityBlockingQueue<>();
    //current running task collection
    private final SparseArray<RequestTask> currentRunningTasks = new SparseArray<>();
    //all download task future
    private SparseArray<Future> futures = new SparseArray<>();

    private NetworkListenerBroadcastReceiver networkListenerBroadcastReceiver = null;
    private SpeedOption mSpeedOption;

    private IDatabase database;

    private AtomicInteger currentRunningTaskCount = new AtomicInteger();

    volatile static boolean DISPATCHER_INIT = false;

    private Dispatcher dispatcher;

    private ITaskGroupDownloadProgressCallback progressCallback = null;
    private ITaskGroupDownloadResultCallback resultCallback = null;

    private ByteArrayPool mPool;

    private int currentNetType = SpeedOption.NETWORK_DEFAULT;

    private RequestTaskQueue(Context context, SpeedOption speedOption){
        mSpeedOption = speedOption;
        mPool = new ByteArrayPool(4096);
        networkListenerBroadcastReceiver = new NetworkListenerBroadcastReceiver(this, context);
        networkListenerBroadcastReceiver.register();
    }

    public ByteArrayPool getPool() {
        return mPool;
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

    /**
     * Record the number of tasks currently being performed
     * @param requestTask running task
     */
    void incrementRunningTaskCount(RequestTask requestTask){
        currentRunningTaskCount.incrementAndGet();
        currentRunningTasks.put(requestTask.getHashCode(), requestTask);
    }

    void decrementRunningTaskCount(RequestTask requestTask){
        currentRunningTaskCount.decrementAndGet();
        currentRunningTasks.remove(requestTask.getHashCode());
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

    void clearCompleteTaskAndSelectNew(RequestTask requestTask){
        tasks.remove(requestTask.getUniqueId(), requestTask);
        uniqueKeys.remove(requestTask.getUrl(), requestTask.getUniqueId());
        pollRequestTaskToRunningQueue();
    }

    private void pollRequestTaskToRunningQueue(){
        if(resumeTaskQueue.size() > 0){
            if(canPutTaskToRunningQueue()) {
                runningTaskQueue.put(resumeTaskQueue.poll());
            }
        }else {
            if (canExit() && mSpeedOption.autoExit) {
                autoQuit();
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
            addTaskToResumeQueue(pauseTaskQueue.poll());
        }
    }

    void addTaskToResumeQueue(RequestTask task){
        addTaskToResumeQueue(task, true);
    }

    private void addTaskToResumeQueue(RequestTask task, boolean autoExecutedNext){
        resumeTaskQueue.put(task);
        if(autoExecutedNext){
            pollRequestTaskToRunningQueue();
        }
    }

    private int getCurrentRunningTaskNum() {
        return runningTaskQueue.size() + currentRunningTaskCount.get();
    }

    private boolean canPutTaskToRunningQueue(){
        return getCurrentRunningTaskNum() < mSpeedOption.maxAllowRunningTaskNum
                && Utils.netAllowTaskRunning(currentNetType, mSpeedOption);
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

    void addFuture(int key, Future future){
        futures.put(key, future);
    }

    void removeTaskFuture(int key){
        Future future = futures.get(key);
        if(future != null){
            future.cancel(true);
            futures.remove(key);
        }
    }

    Future getTaskFuture(int key){
        return futures.get(key);
    }

    void addTaskToResumeOrRunningQueue(RequestTask requestTask) {
        tasks.put(requestTask.getUniqueId(), requestTask);
        if(canPutTaskToRunningQueue()){
            runningTaskQueue.put(requestTask);
        } else {
            requestTask.setStatus(Status.RESUME);
            resumeTaskQueue.put(requestTask);
        }
    }

    public RequestTaskQueue setOnTaskGroupDownloadProgressListener(ITaskGroupDownloadProgressCallback progressListener){
        this.progressCallback = progressListener;
        return this;
    }

    public RequestTaskQueue setOnTaskQueueDownloadResultListener(ITaskGroupDownloadResultCallback resultListener){
        this.resultCallback = resultListener;
        return this;
    }

    ITaskGroupDownloadProgressCallback getProgressCallback() {
        return progressCallback;
    }

    ITaskGroupDownloadResultCallback getResultCallback() {
        return resultCallback;
    }

    public void clearTaskQueueCallback(){
        progressCallback = null;
        resultCallback = null;
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
        currentRunningTasks.remove(requestTask.getHashCode());
        autoQuit();
    }

    private void autoQuit(){
        if(canExit() && mSpeedOption.autoExit){
            quit();
        }
    }

    public void start(RequestTask requestTask){
        requestTask.start();
    }

    public void reStart(RequestTask requestTask){
        if(canPutTaskToRunningQueue()){
            runningTaskQueue.put(requestTask);
        } else {
            requestTask.reStart();
        }
    }

    public void pause(RequestTask requestTask){
        requestTask.pause();
    }

    /**
     * Suspend all running tasks,
     * When there is a scene that does not allow the task to be executed,
     * this method will be called, such as the network changes
     *
     * This method will put the task into the waiting execution queue.
     * This is also the only way to change the RUNNING state task directly into the RESUME state task. For internal use,
     * the external normal state RUNNING state needs to be changed to PAUSE and then to RESUME.
     */
   private void pauseAllRunningTask(){
        int len = currentRunningTasks.size();
        for(int i = 0; i < len; i++){
            RequestTask task = currentRunningTasks.get(currentRunningTasks.keyAt(i));
            task.setPriority(RequestTask.CHANGE_NETWORK_PRIORITY);
            task.setStatus(Status.RESUME);
            addTaskToResumeQueue(task, false);
        }
        currentRunningTasks.clear();
        currentRunningTaskCount.set(0);
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

    public void cancelAll(final boolean isQuit) {
        ExecutorManager.newInstance().getBackgroundExecutor().submit(new Runnable() {
            @Override
            public void run() {
                cancelAllTask();
                if (isQuit) {
                    shutDown();
                }
            }
        });
    }

    void dispatchNetworkChange(NetworkInfo info){
        if(mSpeedOption.autoMaxAllowRunningTaskNum){
            Utils.adjustMaxRunningTaskCount(info, mSpeedOption);
        }
        if(info != null){
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_WIMAX:
                case ConnectivityManager.TYPE_ETHERNET:
                    currentNetType = SpeedOption.NETWORK_WIFI;
                    netAllow(SpeedOption.NETWORK_WIFI);
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    currentNetType = SpeedOption.NETWORK_MOBILE;
                    netAllow(SpeedOption.NETWORK_MOBILE);
                    break;
            }
        }
    }

    private void netAllow(int netFlag){
        if(!Utils.netAllowTaskRunning(netFlag, mSpeedOption)){
            pauseAllRunningTask();
        } else {
            pollRequestTaskToRunningQueue();
        }
    }

    private void cancelAllTask(){
        int runningTasks = getCurrentRunningTaskNum();
        if(runningTasks > 0 && currentRunningTasks.size() > 0){
            int len = currentRunningTasks.size();
            for(int i = 0; i < len; i++){
                RequestTask task = currentRunningTasks.get(currentRunningTasks.keyAt(i));
                cancel(task);
            }
        }

        tasks.clear();
        uniqueKeys.clear();
        runningTaskQueue.clear();
        resumeTaskQueue.clear();
        pauseTaskQueue.clear();
        currentRunningTasks.clear();
        futures.clear();
    }

    public void quit(){
        dispatcher.setExit(true);
        cancelAll(true);
    }

    private void shutDown(){
        if(networkListenerBroadcastReceiver != null){
            networkListenerBroadcastReceiver.unregister();
        }

        int len = futures.size();
        for(int i = 0; i < len; i++){
            int key = futures.keyAt(i);
            Future future = futures.get(key);
            if(future != null){
                future.cancel(true);
            }
        }
        futures.clear();

        ExecutorManager.newInstance().getBackgroundExecutor().shutdown();

        if(database != null){
            database.close();
            database = null;
        }
        sRequestTaskQueue = null;
        mSpeedOption = null;
        DISPATCHER_INIT = false;

        clearTaskQueueCallback();

        mPool.clear();
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
