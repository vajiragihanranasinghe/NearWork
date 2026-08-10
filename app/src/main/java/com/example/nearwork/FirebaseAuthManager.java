package com.example.nearwork;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthManager {
    
    private static final String TAG = "FirebaseAuthManager";
    private static FirebaseAuthManager instance;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    
    private FirebaseAuthManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }
    
    public static synchronized FirebaseAuthManager getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthManager();
        }
        return instance;
    }
    
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    
    public void signUp(String email, String password, String name, String phone, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserProfile(user.getUid(), name, email, phone);
                            callback.onSuccess("Account created! Welcome " + name + "!");
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Signup failed";
                        callback.onError(error);
                    }
                }
            });
    }
    
    public void signIn(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Logged in! Welcome back!");
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        callback.onError(error);
                    }
                }
            });
    }
    
    public void signOut() {
        auth.signOut();
    }
    
    private void saveUserProfile(String uid, String name, String email, String phone) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("createdAt", System.currentTimeMillis());
        
        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile saved"))
            .addOnFailureListener(e -> Log.e(TAG, "Error saving profile", e));
    }
    
    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}
