package lk.vajira.nearwork;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ReportDialog {

    private static final String[] REASONS = {
            "Spam or misleading",
            "Harassment or hate speech",
            "Fake account or scam",
            "Sexual or inappropriate content",
            "Violence or dangerous content",
            "Other"
    };

    private static final String[] REASON_KEYS = {
            "spam", "harassment", "fake", "sexual", "violence", "other"
    };

    public static void show(Context ctx, String type, String targetId) {
        new AlertDialog.Builder(ctx)
                .setTitle("Report")
                .setItems(REASONS, (dialog, which) -> {
                    String reasonKey = REASON_KEYS[which];
                    if (which == REASONS.length - 1) {
                        askDetails(ctx, type, targetId, reasonKey);
                    } else {
                        submit(ctx, type, targetId, reasonKey, "");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void askDetails(Context ctx, String type, String targetId, String reasonKey) {
        EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("Describe the issue (optional)");

        LinearLayout wrap = new LinearLayout(ctx);
        wrap.setPadding(48, 24, 48, 0);
        wrap.addView(input);

        new AlertDialog.Builder(ctx)
                .setTitle("Details")
                .setView(wrap)
                .setPositiveButton("Submit", (d, w) ->
                        submit(ctx, type, targetId, reasonKey, input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static void submit(Context ctx, String type, String targetId,
                               String reason, String details) {
        ReportManager.submitReport(type, targetId, reason, details, new ReportManager.ReportCallback() {
            @Override public void onSuccess() {
                Toast.makeText(ctx, "✅ Report submitted. Thank you.", Toast.LENGTH_LONG).show();
            }
            @Override public void onError(String message) {
                Toast.makeText(ctx, "Failed: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void confirmBlock(Context ctx, String userId, String displayName) {
        new AlertDialog.Builder(ctx)
                .setTitle("Block user?")
                .setMessage("Block " + (displayName != null ? displayName : "this user")
                        + "? You won't see their content anymore.")
                .setPositiveButton("Block", (d, w) ->
                        BlockManager.blockUser(userId, new BlockManager.BlockCallback() {
                            @Override public void onSuccess() {
                                Toast.makeText(ctx, "🚫 User blocked", Toast.LENGTH_SHORT).show();
                            }
                            @Override public void onError(String message) {
                                Toast.makeText(ctx, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
