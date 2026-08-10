package com.example.nearwork;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    
    private static UserManager instance;
    private SharedPreferences prefs;
    
    private UserManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }
    
    public void createUser(String name, String phone, String email, String imageBase64) {
        String userId = String.valueOf(System.currentTimeMillis());
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_PROFILE_IMAGE, imageBase64)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply();
    }
    
    public void login(String userId) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply();
    }
    
    public void logout() {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
    
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }
    
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }
    
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    public String getProfileImage() {
        return prefs.getString(KEY_PROFILE_IMAGE, "");
    }
}
