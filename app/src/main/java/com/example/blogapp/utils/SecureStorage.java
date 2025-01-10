package com.example.blogapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

public class SecureStorage {
    private static SecureStorage instance;
    private SharedPreferences sharedPreferences;

    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";
    private static final String USER_ID_KEY = "user_id";

    private SecureStorage(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secure_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized SecureStorage getInstance(Context context) {
        if (instance == null) {
            instance = new SecureStorage(context);
        }
        return instance;
    }
    public void saveAccessToken(String accessToken) {
        sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply();
    }
    public String getAccessToken() {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null);
    }
    public void clearAccessToken() {
        sharedPreferences.edit().remove(ACCESS_TOKEN_KEY).apply();
    }
    public void saveRefreshToken(String refreshToken) {
        sharedPreferences.edit().putString(REFRESH_TOKEN_KEY, refreshToken).apply();
    }
    public String getRefreshToken() {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null);
    }
    public void clearRefreshToken() {
        sharedPreferences.edit().remove(REFRESH_TOKEN_KEY).apply();
    }
    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(USER_ID_KEY, userId).apply();
    }
    public String getUserId() {
        return sharedPreferences.getString(USER_ID_KEY, null);
    }
    public void clearUserId() {
        sharedPreferences.edit().remove(USER_ID_KEY).apply();
    }
}
