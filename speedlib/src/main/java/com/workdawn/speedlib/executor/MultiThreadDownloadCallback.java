package com.workdawn.speedlib.executor;

/**
 * Created on 2018/5/4.
 * @author workdawn
 */
interface MultiThreadDownloadCallback {
    void onDownloading(long alreadyDownloaded, String uniqueId);
}
