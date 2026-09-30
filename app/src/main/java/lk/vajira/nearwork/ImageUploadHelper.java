package lk.vajira.nearwork;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageUploadHelper {

    private static final String TAG = "ImageUploadHelper";
    private static final int MAX_IMAGE_SIZE = 1024;
    private static final String BUCKET = "nearwork-images";

    // ============================================================
    // LEGACY BASE64 METHODS — kept for backward compatibility
    // ============================================================

    public static String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public static Bitmap decodeImageFromBase64(String base64String) {
        try {
            byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding image: " + e.getMessage());
            return null;
        }
    }

    public static List<String> encodeImagesToBase64(List<Bitmap> bitmaps) {
        List<String> encodedImages = new ArrayList<>();
        for (Bitmap bitmap : bitmaps) {
            String encoded = encodeImageToBase64(bitmap);
            if (encoded != null && !encoded.isEmpty()) {
                encodedImages.add(encoded);
            }
        }
        return encodedImages;
    }

    // ============================================================
    // BITMAP UTILS
    // ============================================================

    public static Bitmap compressBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
            float ratio = Math.min((float) MAX_IMAGE_SIZE / width, (float) MAX_IMAGE_SIZE / height);
            int newWidth = Math.round(width * ratio);
            int newHeight = Math.round(height * ratio);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            return compressBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap: " + e.getMessage());
            return null;
        }
    }

    // ============================================================
    // SUPABASE STORAGE UPLOAD
    // ============================================================

    public interface UploadCallback {
        void onSuccess(String publicUrl);
        void onError(String errorMessage);
    }

    /**
     * Upload a Bitmap to Supabase Storage. Returns public URL via callback.
     * Folder examples: "ads", "profiles", "chats"
     */
    public static void uploadBitmapToSupabase(Context context, Bitmap bitmap,
                                              String folder, UploadCallback callback) {
        new Thread(() -> {
            try {
                Bitmap compressed = compressBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressed.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] bytes = baos.toByteArray();

                String fileName = folder + "/" + UUID.randomUUID().toString() + ".jpg";
                String uploadUrl = BuildConfig.SUPABASE_URL
                        + "/storage/v1/object/" + BUCKET + "/" + fileName;

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                RequestBody body = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                        .addHeader("x-upsert", "true")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                int code = response.code();
                String respBody = response.body() != null ? response.body().string() : "";
                response.close();

                if (code == 200 || code == 201) {
                    String publicUrl = BuildConfig.SUPABASE_URL
                            + "/storage/v1/object/public/" + BUCKET + "/" + fileName;
                    Log.i(TAG, "Upload OK: " + publicUrl);
                    if (callback != null) callback.onSuccess(publicUrl);
                } else {
                    Log.e(TAG, "Upload failed: " + code + " " + respBody);
                    if (callback != null) callback.onError("Upload failed: " + code);
                }
            } catch (Exception e) {
                Log.e(TAG, "Upload exception: " + e.getMessage());
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * Convenience: get Bitmap from Uri and upload.
     */
    public static void uploadUriToSupabase(Context context, Uri uri,
                                           String folder, UploadCallback callback) {
        Bitmap bmp = getBitmapFromUri(context, uri);
        if (bmp == null) {
            if (callback != null) callback.onError("Could not read image");
            return;
        }
        uploadBitmapToSupabase(context, bmp, folder, callback);
    }

    /**
     * Delete a previously-uploaded file by its public URL.
     */
    public static void deleteFromSupabase(String publicUrl) {
        if (publicUrl == null || !publicUrl.contains("/" + BUCKET + "/")) return;
        new Thread(() -> {
            try {
                String path = publicUrl.substring(publicUrl.indexOf("/" + BUCKET + "/") + BUCKET.length() + 2);
                String deleteUrl = BuildConfig.SUPABASE_URL
                        + "/storage/v1/object/" + BUCKET + "/" + path;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(deleteUrl)
                        .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                        .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                        .delete()
                        .build();
                Response response = client.newCall(request).execute();
                Log.i(TAG, "Delete status: " + response.code());
                response.close();
            } catch (Exception e) {
                Log.e(TAG, "Delete exception: " + e.getMessage());
            }
        }).start();
    }
}
