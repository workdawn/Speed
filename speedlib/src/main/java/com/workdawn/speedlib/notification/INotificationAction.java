package com.workdawn.speedlib.notification;

import android.app.PendingIntent;
import android.content.Context;

import com.workdawn.speedlib.load.RequestTask;

/**
 * Created on 2018/5/9.
 * @author workdawn
 */
public interface INotificationAction {

    PendingIntent onAction(RequestTask requestTask, Context context);
}
