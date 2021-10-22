package com.example.appmusic;

import android.app.Application;

public class InternetApp extends Application {
    static InternetApp wifiInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        wifiInstance = this;
    }
    public static synchronized InternetApp getInstance() {
        return wifiInstance;
    }
}
