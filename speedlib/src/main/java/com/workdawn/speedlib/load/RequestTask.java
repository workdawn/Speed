package com.workdawn.speedlib.load;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.workdawn.speedlib.Status;
import com.workdawn.speedlib.callback.IDownloadProgressCallback;
import com.workdawn.speedlib.callback.IDownloadResultCallback;
import com.workdawn.speedlib.core.Speed;
import com.workdawn.speedlib.core.SpeedOption;
import com.workdawn.speedlib.executor.Dispatcher;
import com.workdawn.speedlib.executor.ExecutorManager;
import com.workdawn.speedlib.executor.RequestRunnable;
import com.workdawn.speedlib.model.DownloadCallback;
import com.workdawn.speedlib.notification.NotificationManagerCenter;
import com.workdawn.speedlib.utils.Preconditions;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public class RequestTask implements Comparable<RequestTask>{
    private Status status;
    //The higher the value, the higher the priority
    private int priority;
    private String uniqueId;
    private SpeedOption option;
    private RequestTaskQueue mRequestTaskQueue;
    private String url;
    public ExecutorService executorService;
    private ExecutorManager executorManager;
    private String fileName;
    private AtomicInteger downloadThreadCount = new AtomicInteger();
    private AtomicInteger downloadFailedThreadCount = new AtomicInteger();
    private IDownloadProgressCallback downloadProgressCallback;
    private IDownloadResultCallback downloadResultCallback;
    public final static int HANDLE_DOWNLOAD = 312;
    public final static int HANDLE_PRE_DOWNLOAD = 311;
    private final static int RE_START_PRIORITY = 1;
    private final static int FAILED_TASK_PRIORITY = 2;
    final static int CHANGE_NETWORK_PRIORITY = 3;
    private final static int NORMAL_TASK_PRIORITY = 0;
    private NotificationManagerCenter managerCenter = null;
    private long sendMsgTime = 0L;
    private long sendNotificationTime = 0L;

    //the number of task failures
    private int taskFailedNum = 0;
    private final static int MAX_ALLOW_TASK_FAILED_NUM = 3;

    private final static int MESSAGE_UPDATE_THRESHOLD = 500;
    private final static int NOTIFICATION_UPDATE_THRESHOLD = 2 * 1000;
    private int HASH_CODE;

    private File saveFile;
    private Map<String, String> headers;

    private Handler h = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_DOWNLOAD:
                    DownloadCallback downloadCallback = (DownloadCallback) msg.obj;
                    long alreadyDownloadSize = downloadCallback.getAlreadyDownloadedBytes();
                    long totalSize = downloadCallback.getTotalBytes();
                    if(mRequestTaskQueue.getProgressCallback() != null){
                        mRequestTaskQueue.getProgressCallback().onTasksDownloading(RequestTask.this.url, totalSize, alreadyDownloadSize);
                    } else if(downloadProgressCallback != null){
                        downloadProgressCallback.onDownloading(totalSize, alreadyDownloadSize);
                    }
                    break;
                case HANDLE_PRE_DOWNLOAD:
                    long totalS = (long) msg.obj;
                    if(downloadProgressCallback != null){
                        downloadProgressCallback.onPreDownload(totalS);
                    }
                    break;
            }
            return false;
        }
    });

    public void sendMessage(int what, Object o){
        if(downloadProgressCallback != null || mRequestTaskQueue.getProgressCallback() != null){
            switch (what) {
                case HANDLE_DOWNLOAD:
                    DownloadCallback d = (DownloadCallback) o;
                    if(System.currentTimeMillis() - sendMsgTime > MESSAGE_UPDATE_THRESHOLD
                            || d.getTotalBytes() == d.getAlreadyDownloadedBytes()){
                        sendMsgTime = System.currentTimeMillis();
                        h.obtainMessage(what, o).sendToTarget();
                    }
                    break;
                case HANDLE_PRE_DOWNLOAD:
                    h.obtainMessage(what, o).sendToTarget();
                    break;
            }
        }
    }

    public void sendNotification(long totalSize , long alreadyDownloadSize){
        if(System.currentTimeMillis() - sendNotificationTime > NOTIFICATION_UPDATE_THRESHOLD
                || totalSize == alreadyDownloadSize){
            sendNotificationTime = System.currentTimeMillis();
            if (managerCenter != null) {
                managerCenter.updateNotificationProgress((int) ((alreadyDownloadSize / (totalSize * 1.0f)) * 100), getHashCode(), fileName);
            }
        }
    }

    public RequestTaskQueue getRequestTaskQueue() {
        return mRequestTaskQueue;
    }

    Status getStatus() {
        return status;
    }

    void setStatus (Status status){
        this.status = status;
    }

    /**
     * Set task priority, the higher the value, the higher the priority
     */
    public void setPriority(int priority){
        this.priority = priority;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public SpeedOption getOption() {
        return option;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public File getSaveFile(){
        return saveFile;
    }

    public RequestTask setSaveFile(File saveFile) {
        Preconditions.checkArgument(saveFile != null, "RequestTask saveFileDir must not be null");
        this.saveFile = saveFile;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj instanceof RequestTask){
            RequestTask anotherRequestTask = (RequestTask) obj;
            if(anotherRequestTask.getUniqueId().equals(uniqueId)){
                return true;
            }
        }
        return false;
    }

    int getHashCode(){
        if(HASH_CODE == 0) HASH_CODE = hashCode();
        return HASH_CODE;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + uniqueId.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + fileName.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NonNull RequestTask o) {
        return this.priority > o.priority ? -1 : this.priority < o.priority ? 1 : 0;
    }

    public boolean canDownload(){
        return !(status == Status.PAUSE || status == Status.RESUME || status == Status.CANCEL);
    }

    public boolean canPause(){
        return status == Status.RUNNING || status == Status.RESUME;
    }

    public boolean canResume(){
        return status == Status.PAUSE;
    }

    public RequestTask(Context context, String url, String fileName, String uniqueId, SpeedOption option){
        this.uniqueId = uniqueId;
        this.option = option;
        this.url = url;
        this.fileName = fileName;
        saveFile = new File(Speed.getFileSaveDir(), fileName);
        executorManager = ExecutorManager.newInstance();
        if(option.showNotification){
            managerCenter = new NotificationManagerCenter(context, option.iNotificationShow, option.iNotificationAction);
            managerCenter.createNotification(this, context);
        }
    }

    public void setRequestTaskQueue(RequestTaskQueue mRequestTaskQueue) {
        if(this.mRequestTaskQueue == null){
            this.mRequestTaskQueue = mRequestTaskQueue;
        }
    }

    public void createExecutorService(int nThreads){
        executorService = mRequestTaskQueue.createExecutorService();
    }

    public RequestTask setOnDownloadProgressChangeListener(IDownloadProgressCallback cb){
        this.downloadProgressCallback = cb;
        return this;
    }

    public RequestTask setOnDownloadResultListener(IDownloadResultCallback cb){
        downloadResultCallback = cb;
        return this;
    }

    public RequestTask setRequestHeaders(Map<String, String> headers){
        this.headers = headers;
        return this;
    }

    public Map<String, String> getRequestHeaders(){
        if(headers != null && headers.size() > 0){
            return headers;
        } else if(option.headers != null && option.headers.size() > 0){
            headers = option.headers;
        }
        return headers;
    }

    public void registerDownloadThread(){
        downloadThreadCount.incrementAndGet();
    }

    public synchronized void unregisterDownloadThread(boolean downloadFailed){
        int count = downloadThreadCount.decrementAndGet();
        if(downloadFailed){
            downloadFailedThreadCount.incrementAndGet();
        }
        if(count == 0 && downloadFailedThreadCount.get() > 0){
            processDownloadFailed(fileName + " download failed");
        }
    }

    public void processDownloadComplete(final String filePath){
        ExecutorManager.newInstance().getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mRequestTaskQueue.getResultCallback() != null) {
                    mRequestTaskQueue.getResultCallback().onTaskComplete(url, filePath);
                } else if (downloadResultCallback != null) {
                    downloadResultCallback.onComplete(filePath);
                }
                clear();
            }
        });
    }

    private void clear(){
        downloadProgressCallback = null;
        downloadResultCallback = null;
        h = null;
        mRequestTaskQueue.decrementRunningTaskCount(this);
        mRequestTaskQueue.removeTaskFuture(getHashCode());
        mRequestTaskQueue.clearCompleteTaskAndSelectNew(this);
    }

    public void processDownloadFailed(final String message){
        ExecutorManager.newInstance().getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mRequestTaskQueue.getResultCallback() != null) {
                    mRequestTaskQueue.getResultCallback().onTaskError(url, message);
                } else if (downloadResultCallback != null) {
                    downloadResultCallback.onError(message);
                }
                handleFailedTask();
            }
        });
    }

    private void handleFailedTask(){
        mRequestTaskQueue.decrementRunningTaskCount(this);
        if(taskFailedNum < MAX_ALLOW_TASK_FAILED_NUM){
            status = Status.RESUME;
            priority = FAILED_TASK_PRIORITY;
            mRequestTaskQueue.addTaskToResumeQueue(this);
            taskFailedNum ++;
        } else {
            status = Status.PAUSE;
            priority = NORMAL_TASK_PRIORITY;
            mRequestTaskQueue.addTaskToPauseQueue(this);
            taskFailedNum = 0;
        }
    }

    void start(){
        mRequestTaskQueue.addTaskToResumeOrRunningQueue(this);
        if(!RequestTaskQueue.DISPATCHER_INIT){
            RequestTaskQueue.DISPATCHER_INIT = true;
            Dispatcher dispatcher = new Dispatcher(mRequestTaskQueue);
            mRequestTaskQueue.setDispatcher(dispatcher);
            mRequestTaskQueue.addFuture(-1, executorManager .getBackgroundExecutor().submit(dispatcher));
        }
    }

    void reStart(){
        priority = RE_START_PRIORITY;
    }

    void pause(){
        status = Status.PAUSE;
        mRequestTaskQueue.decrementRunningTaskCount(this);
        mRequestTaskQueue.addTaskToPauseQueue(this);
    }

    void resume(){
        status = Status.RESUME;
        mRequestTaskQueue.addPauseTaskToResumeQueue();
    }

    void cancel(){
        status = Status.CANCEL;
        mRequestTaskQueue.removeRequestTask(this);
        clear();
    }

    public void run(){
        Future taskFuture = mRequestTaskQueue.getTaskFuture(getHashCode());
        if(taskFuture != null){
            taskFuture.cancel(true);
            mRequestTaskQueue.removeTaskFuture(getHashCode());
        }
        status = Status.RUNNING;
        mRequestTaskQueue.incrementRunningTaskCount(this);
        mRequestTaskQueue.addFuture(getHashCode(), executorManager.getBackgroundExecutor()
                .submit(new RequestRunnable(this, mRequestTaskQueue.getDatabase())));
    }
}
