package com.workdawn.speedlib.core;


import android.content.Context;
import android.support.annotation.Nullable;

import com.workdawn.speedlib.Status;
import com.workdawn.speedlib.callback.DownloadRequestSettingCallback;
import com.workdawn.speedlib.db.IDatabase;
import com.workdawn.speedlib.load.IHttpClient;
import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.load.RequestTaskQueue;
import com.workdawn.speedlib.load.RequestTaskWrapper;
import com.workdawn.speedlib.utils.LogUtils;
import com.workdawn.speedlib.utils.Preconditions;
import com.workdawn.speedlib.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use entrance
 * Created on 2018/4/25.
 * @author workdawn
 */
public class Speed {
    private static SpeedOption sDefaultOption = null;
    private static volatile boolean isInit = false;
    private static Context sContext;
    private static RequestTaskQueue sRequestTaskQueue;

    private Speed(){}

    /**
     * Init Speed with default SpeedOption
     */
    public static void init(Context context){
        init(context, SpeedOption.newInstance());
    }

    /**
     * Init Speed with special SpeedOption
     */
    public static void init(Context context, SpeedOption option){
        Preconditions.checkNotNull(context, "Context must not be null");
        Preconditions.checkNotNull(option, "SpeedOption must not be null");
        isInit = true;
        sContext = context.getApplicationContext();
        sDefaultOption = option;
        LogUtils.openLog(sDefaultOption.showLog);
        sRequestTaskQueue = RequestTaskQueue.newInstance(sContext, sDefaultOption);
    }

    /**
     * Get database
     * @return DatabaseImpl
     */
    public static IDatabase getDatabase(){
        checkInit();
        return sDefaultOption.databaseFactory.create(sContext);
    }

    /**
     * Get httpClient
     * @return HttpClientImpl
     */
    public static IHttpClient getHttpClient(){
        checkInit();
        return sDefaultOption.httpClientFactory.create();
    }

    /**
     * Get SpeedOption
     * @return SpeedOption
     */
    public static SpeedOption getSpeedOption(){
        checkInit();
        return sDefaultOption;
    }

    private static void checkInit(){
        if(!isInit) throw new RuntimeException("Speed must init first ! have you call init() ?");
    }

    /**
     * Start download
     * @param url resource download address
     * @return requestTask or null
     */
    public static RequestTask start(String url){
        return start(url, null);
    }

    /**
     * Start download
     * @param url resource download address
     * @param fileName file name with extension
     * @return requestTask or null
     */
    public static RequestTask start(String url, String fileName){
        return start(url, fileName, null);
    }

    /**
     * Start download
     * @param url resource download address
     * @param fileName file name with extension
     * @param cb task setting callback
     */
    public static RequestTask start(String url, String fileName, DownloadRequestSettingCallback cb){
        checkInit();
        return realStart(url, fileName, cb);
    }

    private static RequestTask realStart(String url, String fileName, DownloadRequestSettingCallback cb){
        Preconditions.checkArgument(Utils.isUrlCorrect(url), "Incorrect address " + url);
        String uniqueId = sRequestTaskQueue.getUniqueKey(url);
        if(Utils.isStringEmpty(uniqueId)){
            uniqueId = Utils.generateKey(url);
            sRequestTaskQueue.putUniqueKey(url, uniqueId);
        }
        Status status = sRequestTaskQueue.getStatus(uniqueId);
        RequestTask task = sRequestTaskQueue.getRequestTask(uniqueId);
        if(null != task){
            if(Status.RUNNING == status){
                LogUtils.w("Task already start, do not start again");
                return task;
            }
            if(Status.PAUSE == status){
                task.setStatus(Status.RESUME);
                task.setPriority(RequestTask.RE_START_PRIORITY);
                sRequestTaskQueue.removeAndAddTaskToResumeQueue(task);
                return task;
            }

            //current task status is RESUME
            sRequestTaskQueue.reStart(task);
        } else {
            if(Utils.isStringEmpty(fileName)){
                fileName = url.substring(url.lastIndexOf("/") + 1);
                if(fileName.length() > 20){
                    fileName = fileName.substring(0, 15);
                }
                fileName = fileName + "_" + UUID.randomUUID().toString() + ".tmp";
            }
            task = new RequestTask(sContext, url, fileName, uniqueId, sDefaultOption);
            task.setRequestTaskQueue(sRequestTaskQueue);
            if(cb != null){
                RequestTaskWrapper taskWrapper = RequestTaskWrapper.getInstance();
                cb.requestParamsSetting(taskWrapper.wrapperTask(task));
                sRequestTaskQueue.setRequestTaskWrapper(taskWrapper);
            }
            sRequestTaskQueue.start(task);
        }
        return task;
    }

    /**
     * Start a group of tasks
     * @param urls tasks url
     * @return taskQueue
     */
    public static RequestTaskQueue start(ArrayList<String> urls){
        return start(urls, null);
    }

    /**
     * Start a group of tasks
     * @param urls tasks url
     * @param fileNames tasks file name
     * @return taskQueue
     */
    public static RequestTaskQueue start(ArrayList<String> urls, ArrayList<String> fileNames){
        return start(urls, fileNames, null);
    }


    /**
     * Start a group of tasks
     * @param urls tasks url
     * @param fileNames tasks file name
     * @param cb task setting callback
     */
    public static RequestTaskQueue start(ArrayList<String> urls, ArrayList<String> fileNames, DownloadRequestSettingCallback cb){
        checkInit();
        Preconditions.checkArgument(urls != null && urls.size() > 0, "Group of task urls is empty");
        if(fileNames != null){
            Preconditions.checkArgument(urls.size() == fileNames.size(), "The task address should correspond to the task name");
        }
        int len = urls.size();
        for(int i = 0; i < len; i ++){
            String url = urls.get(i);
            String fileName = null;
            if(fileNames != null){
                fileName = fileNames.get(i);
            }
            realStart(url, fileName, cb);
        }
        return sRequestTaskQueue;
    }

    /**
     * Pause requestTask
     * @param url resource download address
     * @return requestTask or null
     */
    @Nullable
    public static RequestTask pause(String url){
        checkInit();
        Preconditions.checkArgument(Utils.isUrlCorrect(url), "Incorrect address " + url);
        String uniqueId = sRequestTaskQueue.getUniqueKey(url);
        RequestTask task = null;
        if(Utils.isStringEmpty(uniqueId)){
            LogUtils.w("Did not find the task corresponding to the URL, is the URL spelled correctly?");
        } else {
            task = sRequestTaskQueue.getRequestTask(uniqueId);
            if(!task.canPause()){
                LogUtils.w("Only running or resume requestTask can pause !!!");
            } else {
                sRequestTaskQueue.pause(task);
            }
        }
        return task;
    }

    /**
     * Resume requestTask
     * @param url resource download address
     * @return requestTask or null
     */
    @Nullable
    public static RequestTask resume(String url){
        checkInit();
        Preconditions.checkArgument(Utils.isUrlCorrect(url), "Incorrect address " + url);
        String uniqueId = sRequestTaskQueue.getUniqueKey(url);
        RequestTask task = null;
        if(Utils.isStringEmpty(uniqueId)){
            LogUtils.w("Did not find the task corresponding to the URL, is the URL spelled correctly?");
        } else {
            task = sRequestTaskQueue.getRequestTask(uniqueId);
            if(!task.canResume()){
                LogUtils.w("Only pause requestTask can resume !!!");
            } else {
                sRequestTaskQueue.resume(task);
            }
        }
        return task;
    }

    /**
     * Cancel requestTask
     *
     * The difference with {@link #pause(String)} is that it will clear the current
     * task from the task queue and pause and exit the thread pool of the current task.
     *
     * @param url resource download address
     * @return requestTask or null
     */
    @Nullable
    public static RequestTask cancel(String url){
        checkInit();
        Preconditions.checkArgument(Utils.isUrlCorrect(url), "Incorrect address " + url);
        String uniqueId = sRequestTaskQueue.getUniqueKey(url);
        if(Utils.isStringEmpty(uniqueId)){
            return null;
        }
        RequestTask task = sRequestTaskQueue.getRequestTask(uniqueId);
        sRequestTaskQueue.cancel(task);
        return task;
    }

    /**
     * Cancel a group of requestTask, group should not be too large
     * @param urls url group
     */
    public static void cancel(List<String> urls){
        checkInit();
        Preconditions.checkArgument((urls != null && urls.size() > 0), "task urls must not be null");
        List<RequestTask> tasks = new ArrayList<>();
        for (String url : urls) {
            String uniqueId = sRequestTaskQueue.getUniqueKey(url);
            if(!Utils.isStringEmpty(uniqueId)){
                tasks.add(sRequestTaskQueue.getRequestTask(uniqueId));
            }
        }
        if(tasks.size() > 0){
            sRequestTaskQueue.cancel(tasks);
        }
    }

    /**
     * Cancel all requestTask
     * This method will empty all task queues
     */
    public static void cancelAll(){
        checkInit();
        sRequestTaskQueue.cancelAll(false);
    }

    /**
     * Exit speed framework, When this method is called, the speed framework will completely exit.
     * If you want to use speed again, please call {@link #init(Context)} or {@link #init(Context, SpeedOption)} first.
     */
    public static void quit(){
        if(sRequestTaskQueue != null){
            sRequestTaskQueue.quit();
            isInit = false;
        }
    }

    /**
     * Return resources save path
     */
    public static File getFileSaveDir(){
        checkInit();
        File saveDir = sDefaultOption.saveDir;
        if(null == saveDir){
            saveDir = Utils.createFileSaveDir(sContext, "speed_download");
        }
        return saveDir;
    }
}
