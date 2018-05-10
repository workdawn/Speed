package com.workdawn.speedlib.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.workdawn.speedlib.load.RequestTask;

/**
 * Created on 2018/5/8.
 * @author workdawn
 */
public interface INotificationShow {

    int getSmallIcon();

    Bitmap getLargeIcon();

    String getContentTitle();

    String getContent();

    String getDownloadFinishContent();

    boolean getAutoCancel();

    RemoteViews getRemoteViews(RequestTask requestTask, Context context);

    int getRemoteViewTitleViewId();

    int getRemoteViewContentViewId();

    int getRemoteViewProgressBarViewId();
}
