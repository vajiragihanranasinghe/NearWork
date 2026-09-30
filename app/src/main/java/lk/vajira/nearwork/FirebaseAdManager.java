package lk.vajira.nearwork;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseAdManager {
    
    private static final String TAG = "FirebaseAdManager";
    private static FirebaseAdManager instance;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private Context context;
    
    private FirebaseAdManager(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static synchronized FirebaseAdManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseAdManager(context);
        }
        return instance;
    }
    
    // Add ad to Firestore
    public void addAd(AdModel ad, AdCallback callback) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        ad.setUserId(userId);
        
        // Convert to map
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("id", ad.getId());
        adMap.put("title", ad.getTitle());
        adMap.put("description", ad.getDescription());
        adMap.put("category", ad.getCategory());
        adMap.put("price", ad.getPrice());
        adMap.put("tradeType", ad.getTradeType());
        adMap.put("contact", ad.getContact());
        adMap.put("contactType", ad.getContactType());
        adMap.put("latitude", ad.getLatitude());
        adMap.put("longitude", ad.getLongitude());
        adMap.put("userId", ad.getUserId());
        adMap.put("timestamp", ad.getTimestamp());
        adMap.put("condition", ad.getCondition());
        adMap.put("location", ad.getLocation());
        adMap.put("adType", ad.getAdType());
        adMap.put("views", ad.getViews());
        adMap.put("isFeatured", ad.isFeatured());
        adMap.put("isVerified", ad.isVerified());
        adMap.put("exchangeFor", ad.getExchangeFor());
        adMap.put("images", ad.getImages());
        adMap.put("userName", ad.getUserName());
        adMap.put("userPhone", ad.getUserPhone());
        
        db.collection("ads")
            .document(ad.getId())
            .set(adMap)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Ad added successfully");
                callback.onSuccess("Ad posted!");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error adding ad", e);
                callback.onError(e.getMessage());
            });
    }
    
    // Get all ads
    public void getAllAds(AdsCallback callback) {
        db.collection("ads")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<AdModel> ads = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                AdModel ad = doc.toObject(AdModel.class);
                                if (ad != null) {
                                    ads.add(ad);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing ad", e);
                            }
                        }
                        callback.onSuccess(ads);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                }
            });
    }
    
    // Search ads
    public void searchAds(String query, AdsCallback callback) {
        String lowerQuery = query.toLowerCase();
        db.collection("ads")
            .whereGreaterThanOrEqualTo("title", lowerQuery)
            .whereLessThanOrEqualTo("title", lowerQuery + "\uf8ff")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<AdModel> ads = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        try {
                            AdModel ad = doc.toObject(AdModel.class);
                            if (ad != null) {
                                ads.add(ad);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing ad", e);
                        }
                    }
                    callback.onSuccess(ads);
                } else {
                    callback.onError(task.getException().getMessage());
                }
            });
    }
    
    // Get ads by user
    public void getAdsByUser(String userId, AdsCallback callback) {
        db.collection("ads")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<AdModel> ads = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        try {
                            AdModel ad = doc.toObject(AdModel.class);
                            if (ad != null) {
                                ads.add(ad);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing ad", e);
                        }
                    }
                    callback.onSuccess(ads);
                } else {
                    callback.onError(task.getException().getMessage());
                }
            });
    }
    
    // Delete ad
    public void deleteAd(String adId, AdCallback callback) {
        db.collection("ads")
            .document(adId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Also delete images from storage
                callback.onSuccess("Ad deleted!");
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage());
            });
    }
    
    public interface AdCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public interface AdsCallback {
        void onSuccess(List<AdModel> ads);
        void onError(String error);
    }
}
