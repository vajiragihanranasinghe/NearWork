package lk.vajira.nearwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // APPLY LANGUAGE ON START
        applyLanguage();
        
        setContentView(R.layout.activity_splash);
        
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (isLoggedIn) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
    
    private void applyLanguage() {
        try {
            String langCode = getSharedPreferences("LanguagePrefs", MODE_PRIVATE)
                    .getString("language_code", "en");
            
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);
            
            Resources res = getResources();
            Configuration config = res.getConfiguration();
            config.setLocale(locale);
            res.updateConfiguration(config, res.getDisplayMetrics());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
