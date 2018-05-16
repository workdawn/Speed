package com.workdawn.speedlib.callback;

/**
 * Created on 2018/5/16.
 * @author workdawn
 */
public interface ITaskGroupDownloadProgressCallback {
    void onTasksDownloading(String url, long totalSize, long downloadedSize);
}
