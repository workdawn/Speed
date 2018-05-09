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

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private NotificationManagerCenter managerCenter = null;
    private long sendMsgTime = 0L;
    private long sendNotificationTime = 0L;
    private final static int UPDATE_THRESHOLD = 500;
    private File saveFile;
    private Map<String, String> headers;

    private Handler h = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_DOWNLOAD:
                    if(downloadProgressCallback != null){
                        DownloadCallback downloadCallback = (DownloadCallback) msg.obj;
                        long alreadyDownloadSize = downloadCallback.getAlreadyDownloadedBytes();
                        long totalSize = downloadCallback.getTotalBytes();
                        downloadProgressCallback.onDownloading(totalSize, alreadyDownloadSize);
                    }
                    break;
                case HANDLE_PRE_DOWNLOAD:
                    if(downloadProgressCallback != null){
                        long totalSize = (long) msg.obj;
                        downloadProgressCallback.onPreDownload(totalSize);
                    }
                    break;
            }
            return false;
        }
    });

    public void sendMessage(int what, Object o){
        switch (what) {
            case HANDLE_DOWNLOAD:
                DownloadCallback d = (DownloadCallback) o;
                if(System.currentTimeMillis() - sendMsgTime > UPDATE_THRESHOLD
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

    public void sendNotification(long totalSize , long alreadyDownloadSize){
        if(System.currentTimeMillis() - sendNotificationTime > UPDATE_THRESHOLD
                || totalSize == alreadyDownloadSize){
            sendNotificationTime = System.currentTimeMillis();
            if (managerCenter != null) {
                managerCenter.updateNotificationProgress((int) ((alreadyDownloadSize / (totalSize * 1.0f)) * 100), hashCode(), fileName);
            }
        }
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
        this.mRequestTaskQueue = mRequestTaskQueue;
        executorService = Executors.newFixedThreadPool(SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT);
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
            processDownloadFailed(fileName + "download failed");
        }
    }

    public void processDownloadComplete(final String filePath){
        ExecutorManager.newInstance().getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (downloadResultCallback != null) {
                    downloadResultCallback.onComplete(filePath);
                }
                clear();
            }
        });
    }

    private void clear(){
        executorService.shutdown();
        downloadProgressCallback = null;
        downloadResultCallback = null;
        executorService = null;
        h = null;
        mRequestTaskQueue.decrementRunningTaskCount();
        mRequestTaskQueue.pollRequestTaskToRunningQueue(this);
    }

    public void processDownloadFailed(final String message){
        ExecutorManager.newInstance().getCallbackExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (downloadResultCallback != null) {
                    downloadResultCallback.onError(message);
                }
                putFailedTaskToResumeQueue();
            }
        });
    }

    private void putFailedTaskToResumeQueue(){
        mRequestTaskQueue.decrementRunningTaskCount();
        status = Status.RESUME;
        priority = FAILED_TASK_PRIORITY;
        mRequestTaskQueue.addTaskToResumeQueue(this);
    }

    void start(){
        mRequestTaskQueue.addTaskToResumeOrRunningQueue(this);
        if(!RequestTaskQueue.DISPATCHER_INIT){
            RequestTaskQueue.DISPATCHER_INIT = true;
            Dispatcher dispatcher = new Dispatcher(mRequestTaskQueue);
            mRequestTaskQueue.setDispatcher(dispatcher);
            mRequestTaskQueue.addFuture(executorManager .getBackgroundExecutor().submit(dispatcher));
        }
    }

    void reStart(){
        priority = RE_START_PRIORITY;
    }

    void pause(){
        status = Status.PAUSE;
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
        status = Status.RUNNING;
        mRequestTaskQueue.incrementRunningTaskCount();
        mRequestTaskQueue.addFuture(executorManager.getBackgroundExecutor().submit(new RequestRunnable(this, mRequestTaskQueue.getDatabase())));
    }
}