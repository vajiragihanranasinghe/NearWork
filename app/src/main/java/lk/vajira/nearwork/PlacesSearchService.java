package lk.vajira.nearwork;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlacesSearchService {

    private static final String TAG = "PlacesSearchService";
    private static PlacesSearchService instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Context context;

    private PlacesSearchService(Context context) {
        this.context = context;
    }

    public static synchronized PlacesSearchService getInstance(Context context) {
        if (instance == null) {
            instance = new PlacesSearchService(context);
        }
        return instance;
    }

    public void searchPlaces(double lat, double lng, String query, int radius, SearchCallback callback) {
        executor.execute(() -> {
            List<String> results = new ArrayList<>();
            try {
                Log.d(TAG, "===== LOCATIONIQ SEARCH =====");
                Log.d(TAG, "Location: " + lat + ", " + lng);
                Log.d(TAG, "Query: " + query);
                Log.d(TAG, "Radius: " + radius + "m");

                String apiKey = BuildConfig.LOCATIONIQ_KEY;
                if (apiKey == null || apiKey.isEmpty()) {
                    results.add("❌ LocationIQ API key missing.");
                    results.add("💡 Check local.properties + rebuild.");
                    results.add(0, "❌ Configuration error");
                    callback.onSuccess(results);
                    return;
                }

                // Build bounding box
                double latOffset = Math.max(0.02, (double) radius / 111000.0);
                double lngOffset = Math.max(0.02, (double) radius / (111000.0 * Math.cos(Math.toRadians(lat))));

                String minLon = String.format(java.util.Locale.US, "%.6f", lng - lngOffset);
                String minLat = String.format(java.util.Locale.US, "%.6f", lat - latOffset);
                String maxLon = String.format(java.util.Locale.US, "%.6f", lng + lngOffset);
                String maxLat = String.format(java.util.Locale.US, "%.6f", lat + latOffset);
                String viewbox = minLon + "," + minLat + "," + maxLon + "," + maxLat;

                String encodedQuery = URLEncoder.encode(query, "UTF-8");

                // LocationIQ forward geocoding (Nominatim-compatible API)
                String urlString = "https://us1.locationiq.com/v1/search"
                        + "?key=" + apiKey
                        + "&q=" + encodedQuery
                        + "&format=json"
                        + "&limit=30"
                        + "&addressdetails=1"
                        + "&viewbox=" + viewbox
                        + "&bounded=1";

                Log.d(TAG, "URL: " + urlString.replace(apiKey, "***"));

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "NearWork/1.0 (Android)");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "HTTP: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    Log.d(TAG, "Results: " + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject place = jsonArray.getJSONObject(i);

                        String displayName = place.optString("display_name", "");
                        String type = place.optString("type", "");
                        String category = place.optString("class", "");
                        double placeLat = place.optDouble("lat", 0);
                        double placeLng = place.optDouble("lon", 0);
                        double distance = calculateDistance(lat, lng, placeLat, placeLng);

                        if (distance > radius * 1.5) continue;

                        String name = getCleanName(displayName);
                        if (name.isEmpty()) continue;

                        String result = "🏠 " + name;

                        if (distance < 1000) {
                            result += "\n📏 " + (int) distance + "m away";
                        } else {
                            result += "\n📏 " + String.format(java.util.Locale.US, "%.1f", distance / 1000) + "km away";
                        }

                        String label = type != null && !type.isEmpty() ? type.replace("_", " ") : category;
                        if (label != null && !label.isEmpty()) {
                            result += "\n🏷️ " + label;
                        }

                        results.add(result);
                    }
                } else {
                    Log.e(TAG, "HTTP error: " + responseCode);
                    results.add("❌ Search failed (HTTP " + responseCode + ")");
                }
                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
                results.add("❌ Search error: " + e.getMessage());
            }

            if (results.isEmpty()) {
                results.add("❌ No places found");
                results.add("💡 Try: restaurant, cafe, hospital, bank");
            }

            results.add(0, "✅ " + results.size() + " results found");
            callback.onSuccess(results);
        });
    }

    private String getCleanName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        String[] parts = fullName.split(",");
        if (parts.length > 0) {
            String name = parts[0].trim();
            if (name.length() > 60) return name.substring(0, 57) + "...";
            return name;
        }
        return "";
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public interface SearchCallback {
        void onSuccess(List<String> results);
        void onError(String error);
    }
}
