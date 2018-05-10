package com.workdawn.speedlib.model;

/**
 * Created on 2018/5/4.
 * @author workdawn
 */
public class DownloadCallback {
    private long totalBytes;
    private long alreadyDownloadedBytes;

    public DownloadCallback(long totalBytes){
        this.totalBytes = totalBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes){
        this.totalBytes = totalBytes;
    }

    public long getAlreadyDownloadedBytes() {
        return alreadyDownloadedBytes;
    }

    public void setAlreadyDownloadedBytes(long alreadyDownloadedBytes) {
        this.alreadyDownloadedBytes = alreadyDownloadedBytes;
    }
}
