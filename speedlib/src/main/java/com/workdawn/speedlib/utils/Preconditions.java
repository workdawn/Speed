package com.workdawn.speedlib.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created on 2018/4/25.
 * @author workdawn
 */
public class Preconditions {

    private Preconditions(){}

    public static void checkArgument(boolean expression, @NonNull String message){
        if(!expression) throw new IllegalArgumentException(message);
    }

    public static <T> T checkNotNull(@Nullable T arg, @NonNull String message) {
        if (arg == null) {
            throw new NullPointerException(message);
        }
        return arg;
    }
}
