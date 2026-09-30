package lk.vajira.nearwork;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PlaceDetailActivity extends AppCompatActivity {

    TextView placeName, placeAddress, placeRating, placePhone, placeWebsite;
    Button btnDirections, btnFavorite, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        placeName = findViewById(R.id.placeName);
        placeAddress = findViewById(R.id.placeAddress);
        placeRating = findViewById(R.id.placeRating);
        placePhone = findViewById(R.id.placePhone);
        placeWebsite = findViewById(R.id.placeWebsite);
        btnDirections = findViewById(R.id.btnDirections);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);

        String name = getIntent().getStringExtra("place_name");
        placeName.setText(name != null ? name : "Place Name");
        placeAddress.setText("📍 Location details");
        placeRating.setText("⭐⭐⭐⭐⭐ 4.5");
        placePhone.setText("📞 +1 234-567-8900");
        placeWebsite.setText("🌐 www.example.com");

        btnDirections.setOnClickListener(v -> 
            Toast.makeText(this, "🗺️ Opening directions...", Toast.LENGTH_SHORT).show());

        btnFavorite.setOnClickListener(v -> 
            Toast.makeText(this, "⭐ Added to favorites!", Toast.LENGTH_SHORT).show());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + name + " on NearWork!");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }
}
