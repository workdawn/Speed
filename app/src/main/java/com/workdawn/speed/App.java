package com.workdawn.speed;

import android.app.Application;

import com.workdawn.speedlib.core.Speed;
import com.workdawn.speedlib.core.SpeedOption;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpeedOption option = SpeedOption.newInstance()
                .setNotificationActionImpl(new NotificationActionImpl())
                .setNotificationShowImpl(new NotificationShowImpl());
        Speed.init(this, option);
    }
}
