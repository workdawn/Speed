package com.workdawn.speed;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.workdawn.speedlib.load.RequestTask;
import com.workdawn.speedlib.notification.INotificationAction;

public class NotificationActionImpl implements INotificationAction {
    @Override
    public PendingIntent onAction(RequestTask requestTask, Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(requestTask.getSaveFile()), type);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }
}
