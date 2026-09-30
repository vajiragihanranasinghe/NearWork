package lk.vajira.nearwork;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockManager {

    public interface BlockCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface BlockListCallback {
        void onResult(List<String> blockedIds);
        void onError(String message);
    }

    private static String myUid() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public static void blockUser(String userId, BlockCallback cb) {
        String me = myUid();
        if (me == null) { if (cb != null) cb.onError("Not signed in"); return; }
        if (userId.equals(me)) { if (cb != null) cb.onError("Cannot block yourself"); return; }

        Map<String, Object> update = new HashMap<>();
        update.put("blockedUsers", FieldValue.arrayUnion(userId));

        FirebaseFirestore.getInstance()
                .collection("users").document(me)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(v -> { if (cb != null) cb.onSuccess(); })
                .addOnFailureListener(e -> { if (cb != null) cb.onError(e.getMessage()); });
    }

    public static void unblockUser(String userId, BlockCallback cb) {
        String me = myUid();
        if (me == null) { if (cb != null) cb.onError("Not signed in"); return; }

        Map<String, Object> update = new HashMap<>();
        update.put("blockedUsers", FieldValue.arrayRemove(userId));

        FirebaseFirestore.getInstance()
                .collection("users").document(me)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(v -> { if (cb != null) cb.onSuccess(); })
                .addOnFailureListener(e -> { if (cb != null) cb.onError(e.getMessage()); });
    }

    public static void getBlockedUsers(BlockListCallback cb) {
        String me = myUid();
        if (me == null) { if (cb != null) cb.onError("Not signed in"); return; }

        FirebaseFirestore.getInstance()
                .collection("users").document(me).get()
                .addOnSuccessListener(doc -> {
                    List<String> list = new ArrayList<>();
                    if (doc.exists()) {
                        Object raw = doc.get("blockedUsers");
                        if (raw instanceof List) {
                            for (Object o : (List<?>) raw) {
                                if (o instanceof String) list.add((String) o);
                            }
                        }
                    }
                    if (cb != null) cb.onResult(list);
                })
                .addOnFailureListener(e -> { if (cb != null) cb.onError(e.getMessage()); });
    }
}
