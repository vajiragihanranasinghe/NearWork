package lk.vajira.nearwork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PICKER_REQUEST = 1001;

    TextView locationText;
    Button locationButton;
    Button pickLocationButton;
    EditText searchBox;
    Button searchButton;
    Spinner radiusSpinner;
    RecyclerView resultsRecyclerView;
    TextView noResultsText;
    BottomNavigationView bottomNav;
    FloatingActionButton fabMap;
    FloatingActionButton fabPostAd;
    
    FusedLocationProviderClient fusedLocationClient;
    double currentLat = 0.0;
    double currentLng = 0.0;
    boolean hasLocation = false;
    boolean isManualLocation = false;
    
    ArrayList<String> resultsList = new ArrayList<>();
    ArrayList<String> lastSearchResults = new ArrayList<>();
    ResultsAdapter adapter;
    Handler mainHandler = new Handler(Looper.getMainLooper());

    String[] radiusOptions = {
            "100 meters",
            "500 meters",
            "1 km",
            "5 km",
            "10 km",
            "25 km",
            "50 km",
            "100 km"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // ===== UPDATE ALL TEXT =====
        updateAllText();

        locationText = findViewById(R.id.locationText);
        locationButton = findViewById(R.id.locationButton);
        pickLocationButton = findViewById(R.id.pickLocationButton);
        searchBox = findViewById(R.id.searchBox);
        searchButton = findViewById(R.id.searchButton);
        radiusSpinner = findViewById(R.id.radiusSpinner);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        noResultsText = findViewById(R.id.noResultsText);
        bottomNav = findViewById(R.id.bottomNavigation);
        fabMap = findViewById(R.id.fabMap);
        fabPostAd = findViewById(R.id.fabPostAd);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsList = new ArrayList<>();
        adapter = new ResultsAdapter(resultsList, position -> {
            String item = resultsList.get(position);
            if (item != null && !item.startsWith("❌") && !item.startsWith("✅") && !item.startsWith("💡")) {
                Intent intent = new Intent(MainActivity.this, PlaceDetailActivity.class);
                intent.putExtra("place_name", item);
                startActivity(intent);
            }
        });
        resultsRecyclerView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                radiusOptions
        );
        radiusSpinner.setAdapter(spinnerAdapter);

        locationButton.setOnClickListener(v -> getCurrentLocation());
        pickLocationButton.setOnClickListener(v -> openLocationPicker());
        searchButton.setOnClickListener(v -> performSearch());
        
        fabMap.setOnClickListener(v -> {
            if (hasLocation) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("lat", currentLat);
                intent.putExtra("lng", currentLng);
                intent.putExtra("place_name", "Your Location");
                if (!lastSearchResults.isEmpty()) {
                    intent.putStringArrayListExtra("places_list", lastSearchResults);
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, "📍 Set your location first!", Toast.LENGTH_SHORT).show();
            }
        });
        
        fabPostAd.setOnClickListener(v -> {
            if (hasLocation) {
                Intent intent = new Intent(MainActivity.this, PostAdActivity.class);
                intent.putExtra("lat", currentLat);
                intent.putExtra("lng", currentLng);
                startActivity(intent);
            } else {
                Toast.makeText(this, "📍 Set your location first!", Toast.LENGTH_SHORT).show();
            }
        });
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                searchBox.requestFocus();
                return true;
            } else if (id == R.id.nav_favorites) {
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        
        updateResultsVisibility();
    }
    
    private void updateAllText() {
        LanguageHelper lang = LanguageHelper.getInstance(this);
        
        // Update buttons
        locationButton = findViewById(R.id.locationButton);
        if (locationButton != null) locationButton.setText(lang.get("get_my_location"));
        
        pickLocationButton = findViewById(R.id.pickLocationButton);
        if (pickLocationButton != null) pickLocationButton.setText(lang.get("pick_on_map"));
        
        searchButton = findViewById(R.id.searchButton);
        if (searchButton != null) searchButton.setText(lang.get("search_nearby"));
        
        searchBox = findViewById(R.id.searchBox);
        if (searchBox != null) searchBox.setHint(lang.get("search_here"));
        
        noResultsText = findViewById(R.id.noResultsText);
        if (noResultsText != null) noResultsText.setText(lang.get("no_results"));
        
        // Update bottom nav
        bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_home).setTitle(lang.get("home"));
            bottomNav.getMenu().findItem(R.id.nav_search).setTitle(lang.get("search"));
            bottomNav.getMenu().findItem(R.id.nav_favorites).setTitle(lang.get("favorites"));
            bottomNav.getMenu().findItem(R.id.nav_settings).setTitle(lang.get("settings"));
            bottomNav.getMenu().findItem(R.id.nav_profile).setTitle(lang.get("profile"));
        }
    }

    private void performSearch() {
        String query = searchBox.getText().toString().trim();
        String selectedRadius = radiusSpinner.getSelectedItem().toString();

        if (query.isEmpty()) {
            Toast.makeText(this, "🔎 Enter what you're looking for!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasLocation) {
            Toast.makeText(this, "📍 Please set your location first!", Toast.LENGTH_SHORT).show();
            return;
        }

        int radiusMeters = getRadiusInMeters(selectedRadius);
        
        locationText.setText("🔍 Searching...");
        Toast.makeText(this, "🔍 Searching...", Toast.LENGTH_SHORT).show();
        
        noResultsText.setText("⏳ Searching...");
        noResultsText.setVisibility(View.VISIBLE);
        resultsRecyclerView.setVisibility(View.GONE);
        resultsList.clear();
        lastSearchResults.clear();
        
        PlacesSearchService.getInstance(this).searchPlaces(
            currentLat, currentLng, query, radiusMeters,
            new PlacesSearchService.SearchCallback() {
                @Override
                public void onSuccess(List<String> results) {
                    mainHandler.post(() -> {
                        resultsList.clear();
                        if (results != null && !results.isEmpty()) {
                            resultsList.addAll(results);
                            lastSearchResults.addAll(results);
                            
                            int count = 0;
                            for (String p : results) {
                                if (p.startsWith("📍") || p.startsWith("🏪") || p.startsWith("🏠")) count++;
                            }
                            locationText.setText("✅ Found " + count + " places!");
                            Toast.makeText(MainActivity.this, 
                                "✅ Found " + count + " places!", 
                                Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                        updateResultsVisibility();
                    });
                }
                
                @Override
                public void onError(String error) {
                    mainHandler.post(() -> {
                        locationText.setText("❌ Error: " + error);
                        resultsList.add("❌ Search error: " + error);
                        adapter.notifyDataSetChanged();
                        updateResultsVisibility();
                    });
                }
            }
        );
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
            return;
        }

        locationText.setText("📍 Getting location...");
        locationButton.setEnabled(false);

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        locationButton.setEnabled(true);
                        
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            currentLat = location.getLatitude();
                            currentLng = location.getLongitude();
                            hasLocation = true;
                            isManualLocation = false;
                            
                            String lat = String.format("%.6f", currentLat);
                            String lng = String.format("%.6f", currentLng);
                            locationText.setText("📍 GPS Location:\nLat: " + lat + "\nLng: " + lng);
                            Toast.makeText(MainActivity.this, "✅ Location found!", Toast.LENGTH_SHORT).show();
                        } else {
                            locationText.setText("❌ GPS not available\nTry 'Pick on Map'");
                            Toast.makeText(MainActivity.this, "❌ GPS not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openLocationPicker() {
        Intent intent = new Intent(MainActivity.this, LocationPickerActivity.class);
        startActivityForResult(intent, LOCATION_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == RESULT_OK) {
            currentLat = data.getDoubleExtra("selected_lat", 0.0);
            currentLng = data.getDoubleExtra("selected_lng", 0.0);
            hasLocation = true;
            isManualLocation = true;
            
            String lat = String.format("%.6f", currentLat);
            String lng = String.format("%.6f", currentLng);
            locationText.setText("📍 Manual Location:\nLat: " + lat + "\nLng: " + lng);
            Toast.makeText(this, "✅ Location set manually!", Toast.LENGTH_SHORT).show();
        }
    }

    private int getRadiusInMeters(String radiusStr) {
        switch(radiusStr) {
            case "100 meters": return 100;
            case "500 meters": return 500;
            case "1 km": return 1000;
            case "5 km": return 5000;
            case "10 km": return 10000;
            case "25 km": return 25000;
            case "50 km": return 50000;
            case "100 km": return 100000;
            default: return 1000;
        }
    }

    private void updateResultsVisibility() {
        if (resultsList.isEmpty()) {
            resultsRecyclerView.setVisibility(View.GONE);
            noResultsText.setVisibility(View.VISIBLE);
            noResultsText.setText("🔍 Search to find places");
        } else {
            resultsRecyclerView.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "⚠️ Location permission needed!", Toast.LENGTH_LONG).show();
            locationText.setText("⚠️ Permission denied");
        }
    }
}
