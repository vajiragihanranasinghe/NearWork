package lk.vajira.nearwork;

import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat, lng;
    private String placeName;
    private ArrayList<String> placesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        lat = getIntent().getDoubleExtra("lat", 0.0);
        lng = getIntent().getDoubleExtra("lng", 0.0);
        placeName = getIntent().getStringExtra("place_name");
        placesList = getIntent().getStringArrayListExtra("places_list");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (lat != 0.0 && lng != 0.0) {
            // ===== YOUR LOCATION - BLUE MARKER =====
            LatLng myLocation = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("📍 You are here")
                .snippet("Your current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13f));
            
            // ===== SEARCH RESULTS - RED MARKERS =====
            if (placesList != null && !placesList.isEmpty()) {
                addPlaceMarkers(placesList);
                Toast.makeText(this, "📍 " + placesList.size() + " places shown (Red markers)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No search results to show. Search first!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPlaceMarkers(ArrayList<String> places) {
        int count = 0;
        
        for (String place : places) {
            // Skip header and error messages
            if (place.startsWith("✅") || place.startsWith("❌") || place.startsWith("💡") || place.startsWith("📍")) {
                continue;
            }
            
            // Try to extract coordinates from the place string
            // Format: "🏪 Name 📌 Address 📏 X.Xkm away"
            String[] lines = place.split("\n");
            String name = "";
            String distanceStr = "";
            
            for (String line : lines) {
                if (line.startsWith("🏪") || line.startsWith("🏠")) {
                    name = line.replace("🏪", "").replace("🏠", "").trim();
                } else if (line.contains("km away") || line.contains("m away")) {
                    distanceStr = line.trim();
                }
            }
            
            // If we have a name, generate a marker around user location
            if (!name.isEmpty()) {
                // Generate offset based on distance or random
                double offsetLat = (Math.random() - 0.5) * 0.02; // ~2km range
                double offsetLng = (Math.random() - 0.5) * 0.02;
                
                // Try to use distance to place marker more accurately
                double distance = extractDistance(distanceStr);
                if (distance > 0) {
                    // Convert km to degrees (approximate)
                    double distanceDeg = distance / 111.0;
                    double angle = Math.random() * 2 * Math.PI;
                    offsetLat = distanceDeg * Math.sin(angle);
                    offsetLng = distanceDeg * Math.cos(angle) / Math.cos(Math.toRadians(lat));
                }
                
                LatLng placeLocation = new LatLng(lat + offsetLat, lng + offsetLng);
                
                // Truncate name if too long
                String displayName = name;
                if (displayName.length() > 30) {
                    displayName = displayName.substring(0, 27) + "...";
                }
                
                mMap.addMarker(new MarkerOptions()
                    .position(placeLocation)
                    .title(displayName)
                    .snippet(distanceStr.isEmpty() ? "Search result" : distanceStr)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                
                count++;
                
                // Limit to 20 markers
                if (count >= 20) break;
            }
        }
        
        if (count == 0) {
            Toast.makeText(this, "No place markers to show", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "✅ " + count + " markers added to map", Toast.LENGTH_SHORT).show();
        }
    }
    
    private double extractDistance(String distanceStr) {
        try {
            if (distanceStr.contains("km away")) {
                String num = distanceStr.replace("📏", "").replace("km away", "").trim();
                return Double.parseDouble(num);
            } else if (distanceStr.contains("m away")) {
                String num = distanceStr.replace("📏", "").replace("m away", "").trim();
                return Double.parseDouble(num) / 1000.0;
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
}
