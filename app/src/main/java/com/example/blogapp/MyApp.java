package com.example.blogapp;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application {
    private static MyApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        if (instance == null) {
            throw new IllegalStateException("Application is not initialized yet.");
        }
        return instance.getApplicationContext();
    }
}
