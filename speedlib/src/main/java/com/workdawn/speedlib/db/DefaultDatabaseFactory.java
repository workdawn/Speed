package com.workdawn.speedlib.db;

import android.content.Context;

public class DefaultDatabaseFactory implements IDatabaseFactory {
    
    @Override
    public IDatabase create(Context context) {
        return new DefaultDatabaseImpl(context);
    }
}
