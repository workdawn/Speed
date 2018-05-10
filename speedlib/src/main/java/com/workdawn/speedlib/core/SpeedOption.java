package com.workdawn.speedlib.core;


import com.workdawn.speedlib.db.DefaultDatabaseFactory;
import com.workdawn.speedlib.db.IDatabaseFactory;
import com.workdawn.speedlib.load.DefaultHttpClientFactory;
import com.workdawn.speedlib.load.IHttpClientFactory;
import com.workdawn.speedlib.notification.DefaultNotificationActionImpl;
import com.workdawn.speedlib.notification.DefaultNotificationShowImpl;
import com.workdawn.speedlib.notification.INotificationAction;
import com.workdawn.speedlib.notification.INotificationShow;
import com.workdawn.speedlib.utils.Preconditions;

import java.io.File;
import java.util.Map;

/**
 * Some options
 * Created on 2018/4/25.
 * @author workdawn
 */
public class SpeedOption {

    private final static int DEFAULT_CONNECT_TIME_OUT = 30_000;
    private final static int DEFAULT_READ_TIME_OUT = 30_000;
    public final static int DEFAULT_MAX_ALLOW_RUNNING_TASK_COUNT = 3;
    public final static int DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT = 3;

    public IDatabaseFactory databaseFactory = null;
    public IHttpClientFactory httpClientFactory = null;
    public INotificationShow iNotificationShow = null;
    public INotificationAction iNotificationAction = null;
    public int readTimeout = 0;
    public int connectTimeout = 0;
    public boolean showNotification = true;
    public boolean showLog = false;
    public int maxAllowRunningTaskNum;
    public boolean autoMaxAllowRunningTaskNum = false;
    public boolean autoExit = false;
    private static volatile SpeedOption speedOption;
    public String userAgent;
    public File saveDir;
    public Map<String, String> headers;

    private SpeedOption(){
        connectTimeout = DEFAULT_CONNECT_TIME_OUT;
        readTimeout = DEFAULT_READ_TIME_OUT;
        databaseFactory = new DefaultDatabaseFactory();
        httpClientFactory = new DefaultHttpClientFactory();
        showNotification = true;
        maxAllowRunningTaskNum = DEFAULT_MAX_ALLOW_RUNNING_TASK_COUNT;
        autoMaxAllowRunningTaskNum = false;
        showLog = false;
        autoExit = false;
        iNotificationShow = new DefaultNotificationShowImpl();
        iNotificationAction = new DefaultNotificationActionImpl();
    }

    public static SpeedOption newInstance(){
        if(speedOption == null){
            synchronized (SpeedOption.class) {
                if(speedOption == null){
                    speedOption = new SpeedOption();
                }
            }
        }
        return speedOption;
    }

    /**
     * Sets the maximum time in milliseconds to wait while connecting
     * @param timeoutMillis timeMillis to set
     */
    public SpeedOption setConnectTimeout(int timeoutMillis){
        Preconditions.checkArgument(timeoutMillis > 0, "connectTimeout must more than 0");
        this.connectTimeout = timeoutMillis;
        return this;
    }

    /**
     * Sets the maximum time to wait for an input stream read to complete before giving up
     * @param timeoutMillis timeMillis to set
     */
    public SpeedOption setReadTimeOut(int timeoutMillis){
        Preconditions.checkArgument(timeoutMillis > 0, "readTimeout must more than 0");
        this.readTimeout = timeoutMillis;
        return this;
    }

    /**
     * Set DatabaseFactory
     * @param databaseFactory databaseFactory
     */
    public SpeedOption setDatabaseFactory(IDatabaseFactory databaseFactory){
        Preconditions.checkNotNull(databaseFactory, "DatabaseFactory must not be null");
        this.databaseFactory = databaseFactory;
        return this;
    }

    /**
     * Set HttpClientFactory
     * @param httpClientFactory httpClientFactory
     */
    public SpeedOption setHttpClientFactory(IHttpClientFactory httpClientFactory){
        Preconditions.checkNotNull(httpClientFactory, "HttpClientFactory must not be null");
        this.httpClientFactory = httpClientFactory;
        return this;
    }

    /**
     * Set notification implementation class, this setting will only take effect when showNotification = true
     * @param iNotificationShow notificationImpl
     */
    public SpeedOption setNotificationShowImpl(INotificationShow iNotificationShow){
        Preconditions.checkNotNull(iNotificationShow, "notification implementation class must not be null");
        this.iNotificationShow = iNotificationShow;
        return this;
    }

    /**
     *Set notification action after click, this setting will only take effect when showNotification = true
     * @param iNotificationAction NotificationActionImpl
     */
    public SpeedOption setNotificationActionImpl(INotificationAction iNotificationAction){
        Preconditions.checkNotNull(iNotificationAction, "notificationAction implementation class must not be null");
        this.iNotificationAction = iNotificationAction;
        return this;
    }

    /**
     * Whether to display download notification in the notification bar
     * @param show show or not
     */
    public SpeedOption showNotification(boolean show){
        showNotification = show;
        return this;
    }

    /**
     * Whether to show log
     * @param show show or not
     */
    public SpeedOption showLog(boolean show){
        showLog = show;
        return this;
    }

    /**
     * Set the maximum number of tasks that can run simultaneously
     * @param maxNum the max number
     */
    public SpeedOption setMaxAllowRunningTaskCount(int maxNum){
        Preconditions.checkArgument(maxNum > 0, "maxAllowRunningTaskNum must be more than 0");
        maxAllowRunningTaskNum = maxNum;
        return this;
    }

    /**
     *  Whether to automatically set the maximum number of tasks to run
     *  if set, then {@link #setMaxAllowRunningTaskCount(int)} will be ignored , default false
     * @param auto auto or not
     */
    public SpeedOption setAutoMaxAllowRunningTaskCount(boolean auto){
        autoMaxAllowRunningTaskNum = auto;
        return this;
    }

    /**
     * Set userAgent
     * @param agent agent
     */
    public SpeedOption setUserAgent(String agent){
        userAgent = agent;
        return this;
    }

    /**
     * Set the save directory of the download file
     * @param saveDir directory
     */
    public SpeedOption setDownloadDir(File saveDir){
        this.saveDir = saveDir;
        return this;
    }

    /**
     * Set whether to exit Speed automatically when all tasks are completed.
     * The default does not exit automatically. If you set automatic exit,
     * you need to re-invoke the Speed.init() method before calling any method. next time
     */
    public SpeedOption setAutoExitSpeedWhenAllTaskComplete(boolean exit){
        autoExit = exit;
        return this;
    }

    /**
     * Set the unified request header of the Speed framework
     * @param headers request headers
     */
    public SpeedOption setRequestHeaders(Map<String, String> headers){
        Preconditions.checkArgument((headers != null && headers.size() > 0), "Request headers must not be null");
        this.headers = headers;
        return this;
    }
}
