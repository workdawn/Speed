package com.workdawn.speedlib.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.workdawn.speedlib.load.RequestTask;

/**
 * Created on 2018/5/8.
 * @author workdawn
 */
public class DefaultNotificationShowImpl implements INotificationShow {

    private final static String DEFAULT_CONTENT_TITLE = "开始下载： ";
    private final static String DEFAULT_CONTENT = "正在下载";
    private final static String DEFAULT_DOWNLOAD_FINISH_CONTENT = "下载完成";
    @Override
    public int getSmallIcon() {
        return android.R.drawable.stat_sys_download;
    }

    @Override
    public Bitmap getLargeIcon(){
        return null;
    }

    @Override
    public String getContentTitle() {
        return DEFAULT_CONTENT_TITLE;
    }

    @Override
    public String getContent() {
        return DEFAULT_CONTENT;
    }

    @Override
    public String getDownloadFinishContent(){
        return DEFAULT_DOWNLOAD_FINISH_CONTENT;
    }

    @Override
    public boolean getAutoCancel() {
        return true;
    }

    @Override
    public RemoteViews getRemoteViews(RequestTask requestTask, Context context) {
        return null;
    }

    @Override
    public int getRemoteViewTitleViewId() {
        return 0;
    }

    @Override
    public int getRemoteViewContentViewId() {
        return 0;
    }

    @Override
    public int getRemoteViewProgressBarViewId() {
        return 0;
    }
}
