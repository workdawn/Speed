package com.workdawn.speedlib.callback;

/**
 * Created on 2018/5/4.
 * @author workdawn
 */
public interface IDownloadResultCallback {
    void onComplete(String filePath);

    void onError(String reason);
}
