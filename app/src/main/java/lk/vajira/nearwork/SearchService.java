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

public class SearchService {
    
    private static final String TAG = "SearchService";
    private static SearchService instance;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private SearchService() {}
    
    public static synchronized SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        return instance;
    }
    
    public void searchPlaces(double lat, double lng, String query, int radius, SearchCallback callback) {
        executor.execute(() -> {
            List<String> results = new ArrayList<>();
            
            try {
                Log.d(TAG, "🔍 ===== SEARCH START =====");
                Log.d(TAG, "📍 Location: " + lat + ", " + lng);
                Log.d(TAG, "🔎 Query: " + query);
                Log.d(TAG, "📏 Radius: " + radius + "m");
                
                // Build OSM URL
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                
                // Calculate bounding box
                double latOffset = Math.max(0.05, (double) radius / 111000.0);
                double lngOffset = Math.max(0.05, (double) radius / (111000.0 * Math.cos(Math.toRadians(lat))));
                
                double minLat = lat - latOffset;
                double maxLat = lat + latOffset;
                double minLon = lng - lngOffset;
                double maxLon = lng + lngOffset;
                
                String urlString = String.format(
                    "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=30&addressdetails=1&bounded=1&viewbox=%f,%f,%f,%f",
                    encodedQuery, minLon, minLat, maxLon, maxLat
                );
                
                Log.d(TAG, "🌐 URL: " + urlString);
                
                // Make request
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "NearWork/1.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "📡 Response: " + responseCode);
                
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONArray jsonArray = new JSONArray(response.toString());
                    Log.d(TAG, "📍 Found: " + jsonArray.length() + " results");
                    
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject place = jsonArray.getJSONObject(i);
                        
                        String displayName = place.optString("display_name", "");
                        String type = place.optString("type", "");
                        String category = place.optString("category", "");
                        double placeLat = place.optDouble("lat", 0);
                        double placeLon = place.optDouble("lon", 0);
                        
                        // Calculate distance
                        double distance = calculateDistance(lat, lng, placeLat, placeLon);
                        
                        // Only include if within radius
                        if (distance > radius) continue;
                        
                        // Clean name
                        String name = getCleanName(displayName);
                        if (name.isEmpty()) continue;
                        
                        String result = "📍 " + name;
                        
                        // Address (skip first part which is the name)
                        String address = getAddress(displayName);
                        if (!address.isEmpty()) {
                            result += "\n📌 " + address;
                        }
                        
                        // Distance
                        if (distance < 1000) {
                            result += "\n📏 " + (int)distance + "m away";
                        } else {
                            result += "\n📏 " + String.format("%.1f", distance/1000) + "km away";
                        }
                        
                        // Category
                        if (!type.isEmpty() && !type.equals("amenity")) {
                            result += "\n🏷️ " + type.replace("_", " ");
                        }
                        
                        results.add(result);
                    }
                } else {
                    Log.e(TAG, "❌ HTTP Error: " + responseCode);
                }
                conn.disconnect();
                
                if (results.isEmpty()) {
                    results.add("❌ No results found");
                    results.add("💡 Try: restaurant, cafe, hospital, bank");
                    results.add("📍 Your location: " + String.format("%.4f", lat) + ", " + String.format("%.4f", lng));
                }
                
                results.add(0, "✅ " + results.size() + " results found");
                callback.onSuccess(results);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error: " + e.getMessage(), e);
                results.add("❌ Search error");
                results.add("💡 Check internet connection");
                callback.onSuccess(results);
            }
        });
    }
    
    private String getCleanName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        String[] parts = fullName.split(",");
        if (parts.length > 0) {
            String name = parts[0].trim();
            if (name.length() > 60) {
                return name.substring(0, 57) + "...";
            }
            return name;
        }
        return "";
    }
    
    private String getAddress(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        String[] parts = fullName.split(",");
        if (parts.length > 1) {
            StringBuilder addr = new StringBuilder();
            for (int i = 1; i < Math.min(parts.length, 4); i++) {
                if (i > 1) addr.append(", ");
                addr.append(parts[i].trim());
            }
            return addr.toString();
        }
        return "";
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
    
    public interface SearchCallback {
        void onSuccess(List<String> results);
        void onError(String error);
    }
}
