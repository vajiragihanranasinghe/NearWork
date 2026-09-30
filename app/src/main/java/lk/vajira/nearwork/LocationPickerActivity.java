package lk.vajira.nearwork;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker selectedMarker;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private TextView locationText;
    private Button confirmButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        locationText = findViewById(R.id.locationText);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        confirmButton.setOnClickListener(v -> {
            if (selectedLat != 0.0 && selectedLng != 0.0) {
                getIntent().putExtra("selected_lat", selectedLat);
                getIntent().putExtra("selected_lng", selectedLng);
                setResult(RESULT_OK, getIntent());
                finish();
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Default to Colombo if no location
        double defaultLat = 6.9271;
        double defaultLng = 79.8612;

        LatLng defaultLocation = new LatLng(defaultLat, defaultLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        // Add marker at default location
        selectedMarker = mMap.addMarker(new MarkerOptions()
                .position(defaultLocation)
                .title("📍 Your selected location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        selectedLat = defaultLat;
        selectedLng = defaultLng;
        locationText.setText("📍 Selected: " + String.format("%.6f", selectedLat) + ", " + String.format("%.6f", selectedLng));

        // Allow user to tap on map to set location
        mMap.setOnMapClickListener(latLng -> {
            // Remove old marker
            if (selectedMarker != null) {
                selectedMarker.remove();
            }

            // Add new marker
            selectedMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("📍 Your selected location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;

            locationText.setText("📍 Selected: " + String.format("%.6f", selectedLat) + ", " + String.format("%.6f", selectedLng));
            Toast.makeText(this, "Location selected! Tap Confirm to use it.", Toast.LENGTH_SHORT).show();
        });

        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
