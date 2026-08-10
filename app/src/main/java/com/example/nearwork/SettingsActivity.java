package com.example.nearwork;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    Spinner languageSpinner;
    Button applyButton;
    TextView statusText;
    LanguageHelper lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lang = LanguageHelper.getInstance(this);

        languageSpinner = findViewById(R.id.languageSpinner);
        applyButton = findViewById(R.id.applyButton);
        statusText = findViewById(R.id.statusText);

        String[] languages = lang.getLanguages();
        String[] languageCodes = lang.getLanguageCodes();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, languages
        );
        languageSpinner.setAdapter(adapter);

        String currentLang = lang.getLanguage();
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                languageSpinner.setSelection(i);
                break;
            }
        }
        
        statusText.setText("Current: " + languages[getSelectedIndex(languageCodes, currentLang)]);

        applyButton.setOnClickListener(v -> {
            int position = languageSpinner.getSelectedItemPosition();
            String selectedLang = languageCodes[position];
            String selectedName = languages[position];
            
            // Save language
            lang.setLanguage(selectedLang);
            
            statusText.setText("✅ Changed to: " + selectedName);
            Toast.makeText(this, "🌐 Language: " + selectedName, Toast.LENGTH_LONG).show();
            
            // Restart app
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }, 500);
        });
    }
    
    private int getSelectedIndex(String[] codes, String current) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) return i;
        }
        return 0;
    }
}
