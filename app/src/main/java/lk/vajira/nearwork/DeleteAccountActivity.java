package lk.vajira.nearwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DeleteAccountActivity extends AppCompatActivity {

    EditText confirmInput;
    Button confirmDeleteButton;
    ProgressBar deleteProgress;
    TextView deleteStatus;
    FirebaseAdManager adManager;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Delete Account");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adManager = FirebaseAdManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        confirmInput = findViewById(R.id.confirmInput);
        confirmDeleteButton = findViewById(R.id.confirmDeleteButton);
        deleteProgress = findViewById(R.id.deleteProgress);
        deleteStatus = findViewById(R.id.deleteStatus);

        confirmInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                confirmDeleteButton.setEnabled("DELETE".contentEquals(s.toString().trim()));
            }
        });

        confirmDeleteButton.setOnClickListener(v -> startDeleteFlow());
    }

    private void startDeleteFlow() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            setStatus("Not signed in.");
            return;
        }

        confirmDeleteButton.setEnabled(false);
        confirmInput.setEnabled(false);
        deleteProgress.setVisibility(View.VISIBLE);
        setStatus("Preparing…");

        String uid = user.getUid();
        deleteAdsForUser(uid);
    }

    private void deleteAdsForUser(String uid) {
        setStatus("Finding your ads…");
        adManager.getAdsByUser(uid, new FirebaseAdManager.AdsCallback() {
            @Override public void onSuccess(List<AdModel> ads) {
                if (ads == null || ads.isEmpty()) {
                    setStatus("No ads to delete.");
                    deleteUserDoc(uid);
                    return;
                }
                setStatus("Deleting " + ads.size() + " ad(s)…");
                deleteAdsSequentially(ads, 0, uid);
            }
            @Override public void onError(String error) {
                // Even if we can't list them, try the other steps.
                setStatus("Could not list ads (" + error + "). Continuing…");
                deleteUserDoc(uid);
            }
        });
    }

    private void deleteAdsSequentially(List<AdModel> ads, int index, String uid) {
        if (index >= ads.size()) {
            setStatus("All ads deleted.");
            deleteUserDoc(uid);
            return;
        }
        AdModel ad = ads.get(index);
        adManager.deleteAd(ad.getId(), new FirebaseAdManager.AdCallback() {
            @Override public void onSuccess(String message) {
                deleteAdsSequentially(ads, index + 1, uid);
            }
            @Override public void onError(String error) {
                // Log and continue — best-effort cleanup
                deleteAdsSequentially(ads, index + 1, uid);
            }
        });
    }

    private void deleteUserDoc(String uid) {
        setStatus("Deleting profile…");
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener(aVoid -> deleteAuthUser())
            .addOnFailureListener(e -> {
                // Continue anyway — auth deletion is the essential part
                setStatus("Profile cleanup issue: " + e.getMessage() + ". Continuing…");
                deleteAuthUser();
            });
    }

    private void deleteAuthUser() {
        setStatus("Deleting account…");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finishDeletion();
            return;
        }
        user.delete()
            .addOnSuccessListener(aVoid -> finishDeletion())
            .addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                    promptReauth();
                } else {
                    setStatus("Could not delete account: " + e.getMessage());
                    deleteProgress.setVisibility(View.GONE);
                    confirmInput.setEnabled(true);
                    confirmDeleteButton.setEnabled(true);
                }
            });
    }

    private void promptReauth() {
        setStatus("Please sign in again to confirm.");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : null;

        EditText pwd = new EditText(this);
        pwd.setHint("Password");
        pwd.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout wrap = new android.widget.FrameLayout(this);
        wrap.setPadding(pad, pad / 2, pad, 0);
        wrap.addView(pwd);

        new AlertDialog.Builder(this)
            .setTitle("Confirm your password")
            .setView(wrap)
            .setPositiveButton("Confirm", (d, w) -> {
                String password = pwd.getText().toString();
                if (email == null || password.isEmpty()) {
                    setStatus("Missing email or password.");
                    return;
                }
                FirebaseAuth.getInstance().getCurrentUser()
                    .reauthenticate(EmailAuthProvider.getCredential(email, password))
                    .addOnSuccessListener(v -> deleteAuthUser())
                    .addOnFailureListener(e ->
                        setStatus("Re-auth failed: " + e.getMessage()));
            })
            .setNegativeButton("Cancel", (d, w) -> setStatus("Cancelled."))
            .show();
    }

    private void finishDeletion() {
        setStatus("Account deleted. Goodbye.");
        deleteProgress.setVisibility(View.GONE);

        // Clear local prefs
        SharedPreferences prefs = getSharedPreferences("LanguagePrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        getSharedPreferences("NearWorkPrefs", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();

        FirebaseAuth.getInstance().signOut();

        Toast.makeText(this, "Your account has been deleted.", Toast.LENGTH_LONG).show();

        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(DeleteAccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 1500);
    }

    private void setStatus(String text) {
        runOnUiThread(() -> deleteStatus.setText(text));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
