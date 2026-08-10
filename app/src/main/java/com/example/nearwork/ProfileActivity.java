package com.example.nearwork;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class ProfileActivity extends AppCompatActivity {

    EditText nameInput, emailInput, phoneInput;
    Button saveButton, logoutButton, loginButton, changePhotoButton;
    ImageView profileImage;
    TextView userStatus;
    UserManager userManager;
    
    boolean isLoggedIn = false;
    Bitmap selectedProfileImage = null;
    String encodedImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userManager = UserManager.getInstance(this);
        
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        loginButton = findViewById(R.id.loginButton);
        userStatus = findViewById(R.id.userStatus);
        profileImage = findViewById(R.id.profileImage);
        changePhotoButton = findViewById(R.id.changePhotoButton);

        checkLoginStatus();

        saveButton.setOnClickListener(v -> saveProfile());
        logoutButton.setOnClickListener(v -> logoutUser());
        loginButton.setOnClickListener(v -> loginUser());
        changePhotoButton.setOnClickListener(v -> pickImage());
        
        profileImage.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(512)
                .maxResultSize(512, 512)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                Bitmap bitmap = ImageUploadHelper.getBitmapFromUri(this, imageUri);
                if (bitmap != null) {
                    selectedProfileImage = bitmap;
                    profileImage.setImageBitmap(bitmap);
                    encodedImage = ImageUploadHelper.encodeImageToBase64(bitmap);
                    Toast.makeText(this, "✅ Profile photo updated!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkLoginStatus() {
        isLoggedIn = userManager.isLoggedIn();
        
        if (isLoggedIn) {
            userStatus.setText("✅ Logged in as: " + userManager.getUserName());
            userStatus.setTextColor(0xFF48bb78);
            nameInput.setText(userManager.getUserName());
            emailInput.setText(userManager.getUserEmail());
            phoneInput.setText(userManager.getUserPhone());
            
            String savedImage = userManager.getProfileImage();
            if (savedImage != null && !savedImage.isEmpty()) {
                Bitmap bitmap = ImageUploadHelper.decodeImageFromBase64(savedImage);
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap);
                }
            }
            
            nameInput.setEnabled(true);
            emailInput.setEnabled(true);
            phoneInput.setEnabled(true);
            saveButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            changePhotoButton.setVisibility(View.VISIBLE);
        } else {
            userStatus.setText("❌ Not logged in");
            userStatus.setTextColor(0xFFfc8181);
            nameInput.setEnabled(true);
            emailInput.setEnabled(true);
            phoneInput.setEnabled(true);
            saveButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            changePhotoButton.setVisibility(View.VISIBLE);
            profileImage.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        userManager.createUser(name, phone, email, encodedImage);
        Toast.makeText(this, "✅ Profile saved with photo!", Toast.LENGTH_SHORT).show();
        checkLoginStatus();
    }

    private void loginUser() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please enter name and phone", Toast.LENGTH_SHORT).show();
            return;
        }
        
        userManager.createUser(name, phone, email, encodedImage);
        Toast.makeText(this, "✅ Logged in successfully!", Toast.LENGTH_SHORT).show();
        checkLoginStatus();
    }

    private void logoutUser() {
        userManager.logout();
        Toast.makeText(this, "👋 Logged out", Toast.LENGTH_SHORT).show();
        profileImage.setImageResource(android.R.drawable.ic_menu_camera);
        checkLoginStatus();
    }
}
