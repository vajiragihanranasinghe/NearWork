package com.example.nearwork;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        applyLanguage();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyLanguage();
    }
    
    private void applyLanguage() {
        try {
            String langCode = getSharedPreferences("LanguagePrefs", MODE_PRIVATE)
                    .getString("language_code", "en");
            
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);
            
            Resources res = getResources();
            Configuration config = res.getConfiguration();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale);
                config.setLocales(new LocaleList(locale));
            } else {
                config.locale = locale;
            }
            
            res.updateConfiguration(config, res.getDisplayMetrics());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
