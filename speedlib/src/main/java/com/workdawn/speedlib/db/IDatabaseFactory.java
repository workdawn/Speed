package com.workdawn.speedlib.db;


import android.content.Context;

public interface IDatabaseFactory {
    IDatabase create(Context context);
}
