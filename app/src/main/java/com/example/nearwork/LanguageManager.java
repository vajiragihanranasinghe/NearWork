package com.example.nearwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import java.util.Locale;

public class LanguageManager {
    
    private static final String PREF_NAME = "LanguagePrefs";
    private static final String KEY_LANGUAGE = "language_code";
    private static final String DEFAULT_LANGUAGE = "en";
    
    private static LanguageManager instance;
    private SharedPreferences prefs;
    
    private LanguageManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized LanguageManager getInstance(Context context) {
        if (instance == null) {
            instance = new LanguageManager(context);
        }
        return instance;
    }
    
    public void setLanguage(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }
    
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }
    
    public void applyLanguage(Context context) {
        String languageCode = getLanguage();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new LocaleList(locale));
        } else {
            config.locale = locale;
        }
        
        resources.updateConfiguration(config, metrics);
        
        // Also update application context
        if (context.getApplicationContext() != context) {
            Resources appResources = context.getApplicationContext().getResources();
            Configuration appConfig = appResources.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appConfig.setLocale(locale);
                appConfig.setLocales(new LocaleList(locale));
            } else {
                appConfig.locale = locale;
            }
            appResources.updateConfiguration(appConfig, appResources.getDisplayMetrics());
        }
    }
}
