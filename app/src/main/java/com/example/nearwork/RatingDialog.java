package com.example.nearwork;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class RatingDialog extends Dialog {
    
    private RatingBar ratingBar;
    private TextView ratingText;
    private Button submitButton, cancelButton;
    private String serviceName;
    
    public RatingDialog(Context context, String serviceName) {
        super(context);
        this.serviceName = serviceName;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rating);
        
        ratingBar = findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ratingText);
        submitButton = findViewById(R.id.submitButton);
        cancelButton = findViewById(R.id.cancelButton);
        
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            String stars = getStars(rating);
            ratingText.setText(stars + " (" + rating + " stars)");
        });
        
        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(getContext(), "Please rate the service", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getContext(), "⭐ Thank you for rating " + serviceName + "!", Toast.LENGTH_LONG).show();
            dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dismiss());
    }
    
    private String getStars(float rating) {
        int fullStars = (int) rating;
        String stars = "";
        for (int i = 0; i < fullStars; i++) {
            stars += "⭐";
        }
        return stars;
    }
}
