package com.workdawn.speedlib.model;


public class DownloadModel {

    //database table field
    public final static String START_RANGE = "start_range";
    public final static String END_RANGE = "end_range";
    public final static String DOWNLOAD_BYTES = "download_bytes";
    public final static String TOTAL_BYTES = "total_bytes";
    public final static String FILE_TOTAL_BYTES = "file_total_bytes";
    public final static String UNIQUE_ID = "unique_id";
    public final static String DOWNLOAD_URL = "download_url";
    public final static String COMPLETE = "complete";
    public final static String THREAD_ID = "thread_id";
    public final static String E_TAG = "e_tag";
    public final static String LAST_MODIFIED = "last_modified";
    public final static String SAVE_PATH = "save_path";
    public final static String FILE_NAME = "file_name";

    private long startRange;
    private long endRange;
    //already download bytes
    private long downloadBytes;
    //must download totalBytes
    private long totalBytes;
    //file totalBytes
    private long fileTotalBytes;
    //task unique id
    private String uniqueId;
    //download path
    private String downloadUrl;
    // 1 -> completed ; 0 -> undone
    private int complete;
    //thread unique id
    private int threadId;
    //whether to support Accept-Range, 1 -> support; 0 -> unSupport
    private int acceptRange;
    //Has the resource changed
    private String eTag;
    private String lastModified;
    private String savePath;
    private String fileName;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public long getStartRange() {
        return startRange;
    }

    public void setStartRange(long startRange) {
        this.startRange = startRange;
    }

    public long getEndRange() {
        return endRange;
    }

    public void setEndRange(long endRange) {
        this.endRange = endRange;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEtag() {
        return eTag;
    }

    public void setEtag(String eTag) {
        this.eTag = eTag;
    }

    public long getFileTotalBytes() {
        return fileTotalBytes;
    }

    public void setFileTotalBytes(long fileTotalBytes) {
        this.fileTotalBytes = fileTotalBytes;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public boolean isComplete() {
        return complete == 1;
    }

    public int getComplete(){
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getDownloadBytes() {
        return downloadBytes;
    }

    public void setDownloadBytes(long downloadBytes) {
        this.downloadBytes = downloadBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
