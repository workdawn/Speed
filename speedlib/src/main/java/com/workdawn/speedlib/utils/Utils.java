package com.workdawn.speedlib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.workdawn.speedlib.ErrorCode;
import com.workdawn.speedlib.core.SpeedOption;
import com.workdawn.speedlib.executor.RequestRunnable;
import com.workdawn.speedlib.load.IHttpClient;
import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.model.DownloadCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public class Utils {

    /**
     * Generate a unique id based on the url address
     * @param url url address
     * @return unique id
     */
    public static String generateKey(String url){
        try {
            byte[] b = MessageDigest.getInstance("MD5").digest(url.getBytes("UTF-8"));
            return new String(b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return generateKeyWhenFailed(url);
    }

    static String generateKeyWhenFailed(String url){
        String linkTail = url.substring(url.lastIndexOf("/") + 1);
        if(linkTail.length() > 20){
            linkTail = linkTail.substring(0, 20);
        }
        return linkTail + UUID.randomUUID().toString();
    }

    public static boolean isStringEmpty(String str){
        return null == str || str.trim().length() == 0;
    }

    public static boolean isUrlCorrect(String url){
        return !isStringEmpty(url) && url.startsWith("http");
    }

    public static void adjustMaxRunningTaskCount(NetworkInfo info, SpeedOption speedOption){
        if (info == null || !info.isConnectedOrConnecting()) {
            speedOption.setMaxAllowRunningTaskCount(SpeedOption.DEFAULT_MAX_ALLOW_RUNNING_TASK_COUNT);
            return;
        }
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_WIMAX:
            case ConnectivityManager.TYPE_ETHERNET:
                speedOption.setMaxAllowRunningTaskCount(4);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:  // 4G
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        speedOption.setMaxAllowRunningTaskCount(3);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS: // 3G
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        speedOption.setMaxAllowRunningTaskCount(2);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS: // 2G
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        speedOption.setMaxAllowRunningTaskCount(1);
                        break;
                    default:
                        speedOption.setMaxAllowRunningTaskCount(SpeedOption.DEFAULT_MAX_ALLOW_RUNNING_TASK_COUNT);
                }
                break;
            default:
                speedOption.setMaxAllowRunningTaskCount(SpeedOption.DEFAULT_MAX_ALLOW_RUNNING_TASK_COUNT);
        }
    }

    public static File createFileSaveDir(Context context, String path){
        File file;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() != null) {
                file = new File(context.getExternalCacheDir().getAbsolutePath() + "/" + path);
            } else {
                file = new File(context.getCacheDir().getAbsolutePath() + "/" + path);
            }
        } else {
            file = new File(context.getCacheDir().getAbsolutePath() + "/" + path);
        }
        if (!file.exists()) {
            if(!file.mkdirs()){
                return null;
            }
        }
        return file;
    }

    public static void createRandomAccessFile(File saveFile, long fileTotalBytes) throws Exception{
        RandomAccessFile downloadFile = new RandomAccessFile(saveFile, "rw");
        downloadFile.setLength(fileTotalBytes);
        downloadFile.close();
    }

    public static boolean saveFile(InputStream inputStream, File saveDir, final RequestTask requestTask, long fileTotalBytes){
        FileOutputStream fileOutputStream = null;
        ByteArrayPool pool = null;
        byte[] buffer = null;
        try {
            fileOutputStream = new FileOutputStream(saveDir);
            pool = requestTask.getRequestTaskQueue().getPool();
            buffer = pool.getBuf(RequestRunnable.DOWNLOAD_BUFFER_SIZE);
            //byte[] buffer = new byte[RequestRunnable.DOWNLOAD_BUFFER_SIZE];
            int len ;
            long alreadyDownloadedSize = 0L;
            DownloadCallback cb = new DownloadCallback(fileTotalBytes);
            while (requestTask.canDownload() && (len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
                alreadyDownloadedSize += len;
                cb.setAlreadyDownloadedBytes(alreadyDownloadedSize);
                requestTask.sendMessage(RequestTask.HANDLE_DOWNLOAD, cb);
                requestTask.sendNotification(fileTotalBytes, alreadyDownloadedSize);
            }
            if(requestTask.canDownload()){
                requestTask.processDownloadComplete(saveDir.getAbsolutePath());
            }
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            requestTask.processDownloadFailed(ErrorCode.ERROR_UNKNOWN, e.getMessage());
        } finally {
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    requestTask.processDownloadFailed(ErrorCode.ERROR_UNKNOWN, e.getMessage());
                }
            }
            if(pool != null){
                pool.returnBuf(buffer);
            }
        }
        return false;
    }


    public static String getETag(IHttpClient httpClient){
        String eTag = httpClient.getHeaderField(RequestRunnable.HTTP_HEADER_KEY_E_TAG);
        return isStringEmpty(eTag) ? "" : eTag;
    }


    public static String getLastModified(IHttpClient httpClient){
        String lastModified = httpClient.getHeaderField(RequestRunnable.HTTP_HEADER_KEY_LAST_MODIFIED);
        return isStringEmpty(lastModified) ? "" : lastModified;
    }

    public static boolean netAllowTaskRunning(int netFlag, SpeedOption option){
        return (option.mAllowedNetworkTypes & netFlag) > 0;
    }
}
