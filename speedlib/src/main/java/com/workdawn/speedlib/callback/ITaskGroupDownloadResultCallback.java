package com.workdawn.speedlib.callback;

/**
 * Created on 2018/5/16.
 * @author workdawn
 */
public interface ITaskGroupDownloadResultCallback {
    void onTaskComplete(String url, String filePath);

    void onTaskError(String url, String reason);
}
