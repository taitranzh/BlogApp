package com.example.blogapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    public static final String BASE_URL = "https://myblog-ajdce8caa2d8ckdp.southeastasia-01.azurewebsites.net";
    public static final int NUMBER_BLOGS_HOT = 3;
    public static final int NUMBER_CATEGORIES = 100;
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String PREF_LOGIN = "LoginPrefs";
    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_LOGIN, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}
