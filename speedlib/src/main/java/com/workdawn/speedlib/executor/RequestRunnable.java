package com.workdawn.speedlib.executor;

import android.util.SparseArray;

import com.workdawn.speedlib.core.Speed;
import com.workdawn.speedlib.core.SpeedOption;
import com.workdawn.speedlib.db.IDatabase;
import com.workdawn.speedlib.load.IHttpClient;
import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.model.DownloadCallback;
import com.workdawn.speedlib.model.DownloadModel;
import com.workdawn.speedlib.model.RequestModel;
import com.workdawn.speedlib.utils.LogUtils;
import com.workdawn.speedlib.utils.Utils;

import java.io.File;
import java.io.InputStream;

/**
 * Created on 2018/4/27.
 * @author workdawn
 */
public class RequestRunnable implements Runnable , MultiThreadDownloadCallback {

    private RequestTask requestTask;
    private SpeedOption speedOption;

    //---- http request method and response code -----
    private final static String HTTP_METHOD_HEAD = "HEAD";
    final static String HTTP_METHOD_GET = "GET";
    public final static String HTTP_HEADER_KEY_E_TAG = "ETag";
    public final static String HTTP_HEADER_KEY_LAST_MODIFIED = "Last-Modified";
    private final static int HTTP_RESPONSE_CODE_304 = 304;
    private final static int HTTP_RESPONSE_CODE_403 = 403;
    private final static int HTTP_RESPONSE_CODE_416 = 416;
    private final static int HTTP_RESPONSE_CODE_406 = 406;

    public final static int DOWNLOAD_BUFFER_SIZE = 4096;
    private String url;
    private DownloadCallback downloadCallback;
    private long fileTotalBytes = -1L;
    private File saveFile;
    private IDatabase database;
    private boolean downloadFinished = false;
    //In the case of multi-threaded download, it indicates the size of the downloaded file
    private long alreadyDownloadedSize;

    public RequestRunnable(RequestTask requestTask, IDatabase database){
        this.requestTask = requestTask;
        this.database = database;
        speedOption = requestTask.getOption();
    }

    @Override
    public void run() {
        SparseArray<DownloadModel> models = database.find(requestTask.getUniqueId());
        String cacheETag = null;
        String cacheLastModified = null;
        boolean hasCacheData = false;
        long blockLength = -1;
        long tmp;
        if(models.size() > 0){
            hasCacheData = true;
            DownloadModel downloadModel = models.valueAt(0);
            cacheETag = downloadModel.getEtag();
            cacheLastModified = downloadModel.getLastModified();
            fileTotalBytes = downloadModel.getFileTotalBytes();
            blockLength = downloadModel.getTotalBytes();
        }
        downloadCallback = new DownloadCallback(fileTotalBytes);
        IHttpClient httpClient = Speed.getHttpClient();
        url = requestTask.getUrl();
        saveFile = requestTask.getSaveFile();
        String eTag;
        String lastModified;
        try {
            sendHttpRequest(httpClient, HTTP_METHOD_HEAD, cacheETag, cacheLastModified);
            eTag = Utils.getETag(httpClient);
            lastModified = Utils.getLastModified(httpClient);
            fileTotalBytes = httpClient.getContentLength();
            if(fileTotalBytes <= 0){
                LogUtils.i("Http contentLength return 0");
                requestTask.processDownloadFailed("contentLength is wrong, please try again");
                return;
            }
            downloadCallback.setTotalBytes(fileTotalBytes);
            requestTask.sendMessage(RequestTask.HANDLE_PRE_DOWNLOAD, fileTotalBytes);
            tmp = fileTotalBytes / SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT;
            blockLength = fileTotalBytes % SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT == 0 ? tmp : tmp + 1;
            if(blockLength <= 0){
                LogUtils.i("Http blockLength error");
                requestTask.processDownloadFailed("block length is wrong");
                return;
            }
            if(hasNoChange(eTag, cacheETag, cacheLastModified, lastModified)){
                //seems that resources no change
                processWhenResourcesNoChange(models, saveFile, fileTotalBytes, database, blockLength, httpClient);
            } else {
                //it seems that resources have been modified or resources have never been downloaded or for other reasons, so remove old data and reDownload
                processWhenResourceChangeOrHasNotStarted(httpClient, saveFile, fileTotalBytes, hasCacheData, database, blockLength, eTag, lastModified);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                int responseCode = httpClient.getResponseCode();
                LogUtils.i("responseCode = " + responseCode);
                if(HTTP_RESPONSE_CODE_304 == responseCode){
                    //resources no change
                    requestTask.sendMessage(RequestTask.HANDLE_PRE_DOWNLOAD, fileTotalBytes);
                    processWhenResourcesNoChange(models, saveFile, fileTotalBytes, database, blockLength, httpClient);
                } else if(HTTP_RESPONSE_CODE_416 == responseCode) {
                    //range error , maybe resources has change or some other reason, so remove old data and reDownload
                    processHttp416(httpClient, hasCacheData);
                } else if (HTTP_RESPONSE_CODE_403 == responseCode || HTTP_RESPONSE_CODE_406 == responseCode){
                    //maybe unfriendly to http HEAD request support
                    try {
                        sendHttpRequest(httpClient, HTTP_METHOD_GET, cacheETag, cacheLastModified);
                        eTag = Utils.getETag(httpClient);
                        lastModified = Utils.getLastModified(httpClient);
                        fileTotalBytes = httpClient.getContentLength();
                        requestTask.sendMessage(RequestTask.HANDLE_PRE_DOWNLOAD, fileTotalBytes);
                        tmp = fileTotalBytes / SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT;
                        blockLength = fileTotalBytes % SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT == 0 ? tmp : tmp + 1;
                        if(hasNoChange(eTag, cacheETag, cacheLastModified, lastModified)){
                            //resources no change
                            processWhenResourcesNoChange(models, saveFile, fileTotalBytes, database, blockLength, httpClient);
                        } else {
                            //it seems that resources have been modified or resources have never been downloaded or for other reasons, so remove old data and reDownload
                            processWhenResourceChangeOrHasNotStarted(httpClient, saveFile, fileTotalBytes, hasCacheData, database, blockLength, eTag, lastModified);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        int code = httpClient.getResponseCode();
                        if(HTTP_RESPONSE_CODE_304 == code){
                            //resources no change
                            requestTask.sendMessage(RequestTask.HANDLE_PRE_DOWNLOAD, fileTotalBytes);
                            processWhenResourcesNoChange(models, saveFile, fileTotalBytes, database, blockLength, httpClient);
                        } else if(HTTP_RESPONSE_CODE_416 == code) {
                            //range error , maybe resources has change or some other reason, so remove old data and reDownload
                            processHttp416(httpClient, hasCacheData);
                        } else {
                            //request error
                            LogUtils.i("Http download failed, responseCode = " + code);
                            requestTask.processDownloadFailed("request error , requestCode = " + code);
                        }
                    }
                } else {
                    //request error
                    LogUtils.i("Http download failed, responseCode = " + responseCode);
                    requestTask.processDownloadFailed("request error , requestCode = " + responseCode);
                    httpClient.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                LogUtils.i("Http download failed, error message = " + e.getMessage());
                requestTask.processDownloadFailed(e1.getMessage());
                httpClient.close();
            }
        }
    }

    private void processHttp416(IHttpClient httpClient, boolean hasCacheData) throws Exception{
        fileTotalBytes = httpClient.getContentLength();
        long tmp = fileTotalBytes / SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT;
        long blockLength = fileTotalBytes % SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT == 0 ? tmp : tmp + 1;
        String eTag = Utils.getETag(httpClient);
        String lastModified = Utils.getLastModified(httpClient);
        requestTask.sendMessage(RequestTask.HANDLE_PRE_DOWNLOAD, fileTotalBytes);
        processWhenResourceChangeOrHasNotStarted(httpClient, saveFile, fileTotalBytes, hasCacheData, database, blockLength, eTag, lastModified);
    }

    private boolean hasNoChange(String eTag, String cacheETag, String cacheLastModified, String lastModified){

        boolean hasETagNoChange = !Utils.isStringEmpty(cacheETag)
                && !Utils.isStringEmpty(eTag) && eTag.equals(cacheETag);

        boolean hasLastModifiedNoChange = !Utils.isStringEmpty(cacheLastModified)
                && !Utils.isStringEmpty(lastModified) && lastModified.equals(cacheLastModified);

        return hasETagNoChange || hasLastModifiedNoChange;
    }

    /**
     * Http full request
     * @param httpClient httpClient
     * @param httpMethod requestMethod HEAD or GET
     * @param cacheETag compare resource cache ids
     * @throws Exception may throw exception
     */
    private InputStream sendHttpRequest(IHttpClient httpClient, String httpMethod, String cacheETag, String cacheLastModified) throws Exception{
        RequestModel requestModel = new RequestModel();
        requestModel.setUrl(url);
        requestModel.setConnectTimeout(speedOption.connectTimeout);
        requestModel.setReadTimeout(speedOption.readTimeout);
        requestModel.setMethod(httpMethod);
        requestModel.setStartRange(0);
        requestModel.setEndRange(-1);
        if(!Utils.isStringEmpty(cacheETag)){
            requestModel.setIf_None_Match(cacheETag);
        }
        if(!Utils.isStringEmpty(cacheLastModified)){
            requestModel.setIf_Modified_Since(cacheLastModified);
        }
        requestModel.setHeaders(requestTask.getRequestHeaders());
        return httpClient.loadData(requestModel);
    }

    @Override
    public synchronized void onDownloading(long alreadyDownloaded, String uniqueId) {
        alreadyDownloadedSize += alreadyDownloaded;
        downloadCallback.setAlreadyDownloadedBytes(alreadyDownloadedSize);
        requestTask.sendMessage(RequestTask.HANDLE_DOWNLOAD, downloadCallback);
        requestTask.sendNotification(fileTotalBytes, alreadyDownloadedSize);
        if(alreadyDownloadedSize >= fileTotalBytes && !downloadFinished){
            downloadFinished = true;
            requestTask.processDownloadComplete(saveFile.getAbsolutePath());
            database.delete(uniqueId);
        }
    }

    private void processWhenResourcesNoChange(SparseArray<DownloadModel> models, File saveFile, long fileTotalBytes,
                                              IDatabase database, long blockLength, IHttpClient httpClient) throws Exception{
        Utils.createRandomAccessFile(saveFile, fileTotalBytes);
        int modelCount = models.size();
        for(int i = 0; i < modelCount; i++){
            int key = models.keyAt(i);
            DownloadModel downloadModel = models.get(key);
            long startRange = downloadModel.getStartRange();
            long endRange = downloadModel.getEndRange();
            long alreadyDownloadedSize = downloadModel.getDownloadBytes();
            int threadId = downloadModel.getThreadId();
            this.alreadyDownloadedSize += alreadyDownloadedSize;
            requestTask.executorService.submit(new DownloadThread(requestTask, speedOption, alreadyDownloadedSize, startRange,
                    endRange, threadId, url, database, saveFile, blockLength, fileTotalBytes, this));
            requestTask.registerDownloadThread();
        }
        httpClient.close();
    }

    private void processWhenResourceChangeOrHasNotStarted(IHttpClient httpClient, File saveFile, long fileTotalBytes,
                                                          boolean hasCacheData, IDatabase database, long blockLength, String eTag, String lastModified) throws Exception{
        boolean isAcceptRange = httpClient.isAcceptRange();
        if(isAcceptRange){
            //support range
            Utils.createRandomAccessFile(saveFile, fileTotalBytes);
            if(hasCacheData){
                if(database.delete(requestTask.getUniqueId())){
                    if(!saveFile.delete()){
                        LogUtils.e("Old file delete failed");
                        requestTask.processDownloadFailed("Old file delete failed");
                        return;
                    }
                } else {
                    LogUtils.e("Old file record delete failed");
                    requestTask.processDownloadFailed("Old file record delete failed");
                    return;
                }

            }
            for(int i = 0; i < SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT; i ++){
                long startRange = i * blockLength;
                long endRange = (i + 1) * blockLength - 1;
                if(i == SpeedOption.DEFAULT_MAX_ALLOW_DOWNLOAD_THREAD_COUNT - 1){
                    endRange = fileTotalBytes;
                }
                DownloadModel downloadModel = new DownloadModel();
                downloadModel.setStartRange(startRange);
                downloadModel.setEndRange(endRange);
                downloadModel.setComplete(0);
                downloadModel.setDownloadBytes(0);
                downloadModel.setDownloadUrl(requestTask.getUrl());
                downloadModel.setEtag(eTag);
                downloadModel.setLastModified(lastModified);
                downloadModel.setFileName(requestTask.getFileName());
                downloadModel.setFileTotalBytes(fileTotalBytes);
                downloadModel.setSavePath(saveFile.getPath());
                downloadModel.setThreadId(i);
                downloadModel.setTotalBytes(blockLength);
                downloadModel.setUniqueId(requestTask.getUniqueId());
                if(!database.insert(downloadModel)){
                    LogUtils.i("Save data to database failed url = " + requestTask.getUrl());
                }
                LogUtils.i("Thread - " + i + " submit");
                requestTask.executorService.submit(new DownloadThread(requestTask, speedOption, 0, startRange,
                        endRange, i, url, database, saveFile, blockLength, fileTotalBytes, this));
                requestTask.registerDownloadThread();
            }
            httpClient.close();

        } else {
            //not support range
            if(saveFile.exists()){
                if(!saveFile.delete()){
                    LogUtils.e("Old file delete failed");
                    requestTask.processDownloadFailed("Old file delete failed");
                    return;
                }
            }

            if(!Utils.saveFile(sendHttpRequest(httpClient, HTTP_METHOD_GET, "", ""), saveFile, requestTask, fileTotalBytes)){
                LogUtils.i("Save file failed");
            } else {
                if(requestTask.canDownload()){
                    LogUtils.i("RequestTask resource name = " + requestTask.getFileName() + " is finished");
                } else {
                    LogUtils.i("RequestTask resource name = " + requestTask.getFileName() + " is stop");
                }
                httpClient.close();
            }
        }
    }
}
