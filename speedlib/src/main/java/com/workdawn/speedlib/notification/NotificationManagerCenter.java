package com.workdawn.speedlib.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.workdawn.speedlib.load.RequestTask;

/**
 * Created on 2018/5/5.
 * @author workdawn
 */

public class NotificationManagerCenter {

    private NotificationCompat.Builder builder = null;
    private NotificationManager notificationManager = null;
    private INotificationShow iNotificationShow;
    private INotificationAction iNotificationAction;
    private RemoteViews remoteViews;

    public NotificationManagerCenter(Context context, INotificationShow iNotificationShow, INotificationAction iNotificationAction){
        builder = new NotificationCompat.Builder(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.iNotificationShow = iNotificationShow;
        this.iNotificationAction = iNotificationAction;
    }

    public void createNotification(RequestTask requestTask, Context context){
        builder.setSmallIcon(iNotificationShow.getSmallIcon())
                .setLargeIcon(iNotificationShow.getLargeIcon())
                .setContentTitle(iNotificationShow.getContentTitle())
                .setContentText(iNotificationShow.getContent())
                .setAutoCancel(iNotificationShow.getAutoCancel());
        PendingIntent pendingIntent = iNotificationAction.onAction(requestTask, context);
        if(pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }
        remoteViews = iNotificationShow.getRemoteViews(requestTask, context);
        if(remoteViews != null){
            builder.setContent(remoteViews);
        }
    }

    public void updateNotificationProgress(int progress, int notificationId, String fileName){
        if(remoteViews != null){
            remoteViews.setTextViewText(iNotificationShow.getRemoteViewTitleViewId(), fileName + " (" + progress + "%)");
            remoteViews.setProgressBar(iNotificationShow.getRemoteViewProgressBarViewId(), 100, progress, false);
        } else {
            builder.setContentTitle(fileName + " (" + progress + "%)");
            builder.setProgress(100, progress, false);
        }
        notificationManager.notify(notificationId, builder.build());
        if(progress >= 100){
            if(remoteViews != null){
                remoteViews.setTextViewText(iNotificationShow.getRemoteViewContentViewId(), iNotificationShow.getDownloadFinishContent());
            } else {
                builder.setContentText(iNotificationShow.getDownloadFinishContent())
                        .setProgress(0, 0, false);
            }
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
