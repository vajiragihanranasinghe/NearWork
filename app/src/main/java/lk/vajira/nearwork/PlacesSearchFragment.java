package lk.vajira.nearwork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlacesSearchFragment extends Fragment {

    private static final int LOCATION_PICKER_REQUEST = 1001;
    private static final int PERM_REQ = 100;

    TextView locationText;
    MaterialButton locationButton, pickLocationButton, searchButton;
    EditText searchBox;
    Spinner radiusSpinner;
    RecyclerView resultsRecyclerView;
    LinearLayout noResultsContainer;
    TextView noResultsText;
    FloatingActionButton fabMap;

    FusedLocationProviderClient fusedLocationClient;
    double currentLat = 0.0;
    double currentLng = 0.0;
    boolean hasLocation = false;

    ArrayList<String> resultsList = new ArrayList<>();
    ArrayList<String> lastSearchResults = new ArrayList<>();
    ResultsAdapter adapter;
    final Handler mainHandler = new Handler(Looper.getMainLooper());

    String[] radiusOptions = {"100 meters","500 meters","1 km","5 km","10 km","25 km","50 km","100 km"};

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_places_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        locationText = v.findViewById(R.id.locationText);
        locationButton = v.findViewById(R.id.locationButton);
        pickLocationButton = v.findViewById(R.id.pickLocationButton);
        searchBox = v.findViewById(R.id.searchBox);
        searchButton = v.findViewById(R.id.searchButton);
        radiusSpinner = v.findViewById(R.id.radiusSpinner);
        resultsRecyclerView = v.findViewById(R.id.resultsRecyclerView);
        noResultsContainer = v.findViewById(R.id.noResultsContainer);
        noResultsText = v.findViewById(R.id.noResultsText);
        fabMap = v.findViewById(R.id.fabMap);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ResultsAdapter(resultsList, position -> {
            String item = resultsList.get(position);
            if (item != null && !item.startsWith("❌") && !item.startsWith("✅") && !item.startsWith("💡")) {
                Intent intent = new Intent(requireContext(), PlaceDetailActivity.class);
                intent.putExtra("place_name", item);
                startActivity(intent);
            }
        });
        resultsRecyclerView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        radiusSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, radiusOptions));

        locationButton.setOnClickListener(x -> getCurrentLocation());
        pickLocationButton.setOnClickListener(x -> openLocationPicker());
        searchButton.setOnClickListener(x -> performSearch());

        fabMap.setOnClickListener(x -> {
            if (hasLocation) {
                Intent i = new Intent(requireContext(), MapActivity.class);
                i.putExtra("lat", currentLat);
                i.putExtra("lng", currentLng);
                i.putExtra("place_name", "Your Location");
                if (!lastSearchResults.isEmpty()) i.putStringArrayListExtra("places_list", lastSearchResults);
                startActivity(i);
            } else {
                Toast.makeText(requireContext(), "📍 Set your location first!", Toast.LENGTH_SHORT).show();
            }
        });

        updateResultsVisibility();
    }

    private void performSearch() {
        String query = searchBox.getText().toString().trim();
        String selectedRadius = radiusSpinner.getSelectedItem().toString();

        if (query.isEmpty()) { Toast.makeText(requireContext(), "🔎 Enter what you're looking for!", Toast.LENGTH_SHORT).show(); return; }
        if (!hasLocation) { Toast.makeText(requireContext(), "📍 Please set your location first!", Toast.LENGTH_SHORT).show(); return; }

        int radiusMeters = getRadiusInMeters(selectedRadius);
        locationText.setText("🔍 Searching…");
        noResultsText.setText("⏳ Searching…");
        noResultsContainer.setVisibility(View.VISIBLE);
        resultsRecyclerView.setVisibility(View.GONE);
        resultsList.clear();
        lastSearchResults.clear();

        PlacesSearchService.getInstance(requireContext()).searchPlaces(
            currentLat, currentLng, query, radiusMeters,
            new PlacesSearchService.SearchCallback() {
                @Override public void onSuccess(List<String> results) {
                    mainHandler.post(() -> {
                        resultsList.clear();
                        if (results != null && !results.isEmpty()) {
                            resultsList.addAll(results);
                            lastSearchResults.addAll(results);
                            int count = 0;
                            for (String p : results) if (p.startsWith("🏠") || p.startsWith("🏪")) count++;
                            locationText.setText("✅ Found " + count + " places!");
                        }
                        adapter.notifyDataSetChanged();
                        updateResultsVisibility();
                    });
                }
                @Override public void onError(String error) {
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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_REQ);
            return;
        }
        locationText.setText("📍 Getting location…");
        locationButton.setEnabled(false);

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            locationButton.setEnabled(true);
            if (task.isSuccessful() && task.getResult() != null) {
                Location loc = task.getResult();
                currentLat = loc.getLatitude();
                currentLng = loc.getLongitude();
                hasLocation = true;
                locationText.setText(String.format("📍 Lat: %.6f\nLng: %.6f", currentLat, currentLng));
                Toast.makeText(requireContext(), "✅ Location found!", Toast.LENGTH_SHORT).show();
            } else {
                locationText.setText("❌ GPS not available. Try 'Pick on Map'");
            }
        });
    }

    private void openLocationPicker() {
        Intent i = new Intent(requireContext(), LocationPickerActivity.class);
        startActivityForResult(i, LOCATION_PICKER_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_PICKER_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null) {
            currentLat = data.getDoubleExtra("selected_lat", 0.0);
            currentLng = data.getDoubleExtra("selected_lng", 0.0);
            hasLocation = true;
            locationText.setText(String.format("📍 Lat: %.6f\nLng: %.6f", currentLat, currentLng));
            Toast.makeText(requireContext(), "✅ Location set!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQ && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(requireContext(), "⚠️ Location permission needed!", Toast.LENGTH_LONG).show();
        }
    }

    private int getRadiusInMeters(String s) {
        switch (s) {
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
            noResultsContainer.setVisibility(View.VISIBLE);
        } else {
            resultsRecyclerView.setVisibility(View.VISIBLE);
            noResultsContainer.setVisibility(View.GONE);
        }
    }
}
