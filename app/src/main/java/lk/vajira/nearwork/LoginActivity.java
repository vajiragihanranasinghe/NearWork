package lk.vajira.nearwork;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput, nameInput, phoneInput;
    Button loginButton, switchButton;
    TextView statusText;
    boolean isLoginMode = false;
    FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = FirebaseAuthManager.getInstance();

        // Check if already logged in
        if (authManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        loginButton = findViewById(R.id.loginButton);
        switchButton = findViewById(R.id.switchButton);
        statusText = findViewById(R.id.statusText);

        // Start in Login mode
        statusText.setText("Login to your account");
        loginButton.setText("🔐 Login");
        nameInput.setVisibility(View.GONE);
        phoneInput.setVisibility(View.GONE);

        switchButton.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            if (isLoginMode) {
                switchButton.setText("Don't have an account? Sign Up");
                loginButton.setText("🔐 Login");
                nameInput.setVisibility(View.GONE);
                phoneInput.setVisibility(View.GONE);
                statusText.setText("Login to your account");
            } else {
                switchButton.setText("Already have an account? Login");
                loginButton.setText("🔐 Create Account");
                nameInput.setVisibility(View.VISIBLE);
                phoneInput.setVisibility(View.VISIBLE);
                statusText.setText("Create a new account");
            }
        });

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLoginMode) {
                // Login
                authManager.signIn(email, password, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(LoginActivity.this, "✅ " + message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(LoginActivity.this, "❌ " + error, Toast.LENGTH_SHORT).show();
                        statusText.setText("❌ " + error);
                    }
                });
            } else {
                // Signup
                String name = nameInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone.isEmpty()) {
                    Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                authManager.signUp(email, password, name, phone, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(LoginActivity.this, "✅ " + message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(LoginActivity.this, "❌ " + error, Toast.LENGTH_SHORT).show();
                        statusText.setText("❌ " + error);
                    }
                });
            }
        });
    }
}
