package lk.vajira.nearwork;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ReportManager {

    private static final String TAG = "ReportManager";
    private static final String COLLECTION = "reports";

    public interface ReportCallback {
        void onSuccess();
        void onError(String message);
    }

    public static void submitReport(String type, String targetId, String reason,
                                    String details, ReportCallback cb) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            if (cb != null) cb.onError("Not signed in");
            return;
        }

        String reporterId = auth.getCurrentUser().getUid();

        Map<String, Object> report = new HashMap<>();
        report.put("type", type);
        report.put("targetId", targetId);
        report.put("reason", reason);
        report.put("details", details != null ? details : "");
        report.put("reporterId", reporterId);
        report.put("status", "pending");
        report.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection(COLLECTION)
                .add(report)
                .addOnSuccessListener(doc -> {
                    Log.i(TAG, "Report submitted: " + doc.getId());
                    if (cb != null) cb.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Report failed: " + e.getMessage());
                    if (cb != null) cb.onError(e.getMessage());
                });
    }
}
