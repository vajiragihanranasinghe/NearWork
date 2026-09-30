package lk.vajira.nearwork;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    // ⚠️ Replace with your actual URLs after creating the neawork-legal repo
    private static final String PRIVACY_URL = "https://vajiragihanranasinghe.github.io/nearwork-legal/privacy.html";
    private static final String TERMS_URL   = "https://vajiragihanranasinghe.github.io/nearwork-legal/terms.html";
    private static final String CONTACT_EMAIL = "vajiragihanranasinghe@gmail.com";

    Spinner languageSpinner;
    Button applyButton, blockedUsersButton, privacyButton, termsButton,
           reportProblemButton, deleteAccountButton;
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
        blockedUsersButton = findViewById(R.id.blockedUsersButton);
        privacyButton = findViewById(R.id.privacyButton);
        termsButton = findViewById(R.id.termsButton);
        reportProblemButton = findViewById(R.id.reportProblemButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

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
            lang.setLanguage(selectedLang);
            statusText.setText("✅ Changed to: " + selectedName);
            Toast.makeText(this, "🌐 Language: " + selectedName, Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }, 500);
        });

        blockedUsersButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, BlockedUsersActivity.class));
        });

        privacyButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL))));

        termsButton.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL))));

        reportProblemButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + CONTACT_EMAIL));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "NearWork app feedback");
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete account?")
                .setMessage("This permanently deletes your profile, ads, and messages. " +
                        "This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> startActivity(new Intent(SettingsActivity.this, DeleteAccountActivity.class)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDeleteAccount() {
        Toast.makeText(this, "Deleting account…", Toast.LENGTH_SHORT).show();

        // Sign out immediately; full server-side deletion is triggered
        // by the "Delete Account" web page (link in Settings).
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("To complete deletion")
                    .setMessage("Your local account is signed out. To fully delete your " +
                            "data, email " + CONTACT_EMAIL + " with subject 'Delete Account'.")
                    .setPositiveButton("OK", (d, w) -> {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        }, 500);
    }

    private int getSelectedIndex(String[] codes, String current) {
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(current)) return i;
        }
        return 0;
    }
}
