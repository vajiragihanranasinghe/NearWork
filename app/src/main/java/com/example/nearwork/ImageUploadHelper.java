package com.example.nearwork;

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

public class ImageUploadHelper {
    
    private static final String TAG = "ImageUploadHelper";
    private static final int MAX_IMAGE_SIZE = 1024; // Max width/height in pixels
    
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
            inputStream.close();
            return compressBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap: " + e.getMessage());
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
}
