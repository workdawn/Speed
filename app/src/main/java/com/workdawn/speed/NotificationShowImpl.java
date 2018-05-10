package com.workdawn.speed;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.notification.INotificationShow;

public class NotificationShowImpl implements INotificationShow {
    @Override
    public int getSmallIcon() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public Bitmap getLargeIcon() {
        return null;
    }

    @Override
    public String getContentTitle() {
        return "开始下载";
    }

    @Override
    public String getContent() {
        return "正在下载";
    }

    @Override
    public String getDownloadFinishContent() {
        return "已经下载完成";
    }

    @Override
    public boolean getAutoCancel() {
        return true;
    }

    @Override
    public RemoteViews getRemoteViews(RequestTask requestTask, Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_notification_content);
        return remoteViews;
    }

    @Override
    public int getRemoteViewTitleViewId() {
        return R.id.remote_tv_title;
    }

    @Override
    public int getRemoteViewContentViewId() {
        return R.id.remote_tv_content;
    }

    @Override
    public int getRemoteViewProgressBarViewId() {
        return R.id.p_remote;
    }
}
