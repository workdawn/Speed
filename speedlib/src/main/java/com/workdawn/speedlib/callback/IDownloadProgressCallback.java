package com.workdawn.speedlib.callback;

/**
 * Monitor file download progress
 * Created on 2018/4/26.
 * @author workdawn
 */
public interface IDownloadProgressCallback {
    /**
     * @param totalSize file total size
     * @param currentSize already downloaded size
     */
    void onDownloading(long totalSize, long currentSize);

    /**
     * This method indicates that the Speed framework is ready for
     * downloading all the conditions and can be downloaded immediately.
     * @param totalSize file total size
     */
    void onPreDownload(long totalSize);
}
