package com.workdawn.speedlib.executor;

import com.workdawn.speedlib.core.Speed;
import com.workdawn.speedlib.core.SpeedOption;
import com.workdawn.speedlib.db.IDatabase;
import com.workdawn.speedlib.load.IHttpClient;
import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.model.DownloadModel;
import com.workdawn.speedlib.model.RequestModel;
import com.workdawn.speedlib.utils.LogUtils;
import com.workdawn.speedlib.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created on 2018/4/30.
 * @author workdawn
 */

public class DownloadThread implements Runnable{

    private RequestTask requestTask;
    private SpeedOption speedOption;
    private long alreadyDownloadedLength;
    private long startRange;
    private long endRange;
    private int threadId;
    private String url;
    private IDatabase database;
    private File saveFile;
    private long blockLength;
    private long fileTotalBytes;
    private MultiThreadDownloadCallback multiThreadDownloadCallback;

    DownloadThread (RequestTask requestTask, SpeedOption speedOption, long alreadyDownloadedLength,
                    long startRange, long endRange, int threadId, String url, IDatabase database,
                    File saveFile, long blockLength, long fileTotalBytes, MultiThreadDownloadCallback multiThreadDownloadCallback){
        this.requestTask = requestTask;
        this.speedOption = speedOption;
        this.alreadyDownloadedLength = alreadyDownloadedLength;
        this.startRange = startRange;
        this.endRange = endRange;
        this.threadId = threadId;
        this.url = url;
        this.database = database;
        this.saveFile = saveFile;
        this.blockLength = blockLength;
        this.fileTotalBytes = fileTotalBytes;
        this.multiThreadDownloadCallback = multiThreadDownloadCallback;
    }

    @Override
    public void run() {
        RequestModel requestModel = new RequestModel();
        requestModel.setUrl(url);
        requestModel.setConnectTimeout(speedOption.connectTimeout);
        requestModel.setReadTimeout(speedOption.readTimeout);
        requestModel.setMethod(RequestRunnable.HTTP_METHOD_GET);
        long realStartRange = startRange + alreadyDownloadedLength;
        requestModel.setStartRange(realStartRange);
        requestModel.setEndRange(endRange);
        requestModel.setHeaders(requestTask.getRequestHeaders());
        RandomAccessFile randomAccessFile = null;
        IHttpClient httpClient = null;
        try {
            LogUtils.i("Thread id = " + threadId + " start download from position " +
                    realStartRange +" to position = " + endRange + " and alreadyDownload size = " + alreadyDownloadedLength);
            httpClient = Speed.getHttpClient();
            InputStream inputStream = httpClient.loadData(requestModel);
            randomAccessFile = new RandomAccessFile(saveFile, "rwd");
            randomAccessFile.seek(realStartRange);
            DownloadModel downloadModel = new DownloadModel();
            downloadModel.setDownloadUrl(url);
            downloadModel.setFileName(requestTask.getFileName());
            String uniqueId = requestTask.getUniqueId();
            downloadModel.setUniqueId(uniqueId);
            downloadModel.setSavePath(saveFile.getPath());
            downloadModel.setThreadId(threadId);
            downloadModel.setTotalBytes(blockLength);
            downloadModel.setStartRange(startRange);
            downloadModel.setEndRange(endRange);
            downloadModel.setFileTotalBytes(fileTotalBytes);
            String eTag = Utils.getETag(httpClient);
            String lastModified = Utils.getLastModified(httpClient);
            downloadModel.setEtag(eTag);
            downloadModel.setLastModified(lastModified);
            downloadModel.setComplete(0);
            byte[] buffer = new byte[RequestRunnable.DOWNLOAD_BUFFER_SIZE];
            int len;
            while (requestTask.canDownload() && (len = inputStream.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, len);
                alreadyDownloadedLength += len;
                downloadModel.setDownloadBytes(alreadyDownloadedLength);
                LogUtils.i("Thread id = " + threadId + " has download " + alreadyDownloadedLength);
                if(multiThreadDownloadCallback != null){
                    multiThreadDownloadCallback.onDownloading(len, uniqueId);
                }
                database.update(downloadModel);
            }
            if(requestTask.canDownload()){
                downloadModel.setComplete(1);
                database.update(downloadModel);
                LogUtils.i("Thread id = " + threadId + " finished");
            } else {
                LogUtils.i("Thread id = " + threadId + " stop");
            }
            requestTask.unregisterDownloadThread(false);
        } catch (Exception e) {
            e.printStackTrace();
            requestTask.unregisterDownloadThread(true);
            LogUtils.i("Thread id = " + threadId + " download failed, error message = " + e.getMessage());
        } finally {
            if(randomAccessFile != null){
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(httpClient != null){
                httpClient.close();
            }
        }

    }
}
