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
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Context context;
    private String API_KEY = "AIzaSyBK6BAkKfNXmtcQMu-5s1z38MZDMnGDo5s";
    
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
                Log.d(TAG, "🔍 ===== GOOGLE PLACES SEARCH =====");
                Log.d(TAG, "📍 Location: " + lat + ", " + lng);
                Log.d(TAG, "🔎 Query: " + query);
                Log.d(TAG, "📏 Radius: " + radius + "m");
                Log.d(TAG, "🔑 API Key: " + API_KEY.substring(0, 15) + "...");
                
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                
                // Using Text Search for better results
                String urlString = String.format(
                    "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&location=%f,%f&radius=%d&key=%s",
                    encodedQuery, lat, lng, Math.min(radius, 50000), API_KEY
                );
                
                Log.d(TAG, "🌐 URL: " + urlString);
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "📡 Response Code: " + responseCode);
                
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
                    
                    String jsonStr = response.toString();
                    Log.d(TAG, "📄 Response: " + jsonStr.substring(0, Math.min(200, jsonStr.length())));
                    
                    JSONObject json = new JSONObject(jsonStr);
                    String status = json.optString("status", "");
                    Log.d(TAG, "📊 Status: " + status);
                    
                    if (status.equals("OK")) {
                        JSONArray resultsArray = json.getJSONArray("results");
                        Log.d(TAG, "📍 Found: " + resultsArray.length() + " Google Places results");
                        
                        for (int i = 0; i < Math.min(resultsArray.length(), 30); i++) {
                            JSONObject place = resultsArray.getJSONObject(i);
                            
                            String name = place.optString("name", "Unknown");
                            String address = place.optString("formatted_address", "");
                            double rating = place.optDouble("rating", 0);
                            
                            JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                            double placeLat = location.optDouble("lat", 0);
                            double placeLng = location.optDouble("lng", 0);
                            
                            JSONArray types = place.optJSONArray("types");
                            String category = "";
                            if (types != null && types.length() > 0) {
                                category = types.getString(0).replace("_", " ");
                            }
                            
                            String result = "🏪 " + name;
                            if (!address.isEmpty()) {
                                result += "\n📌 " + address;
                            }
                            
                            double distance = calculateDistance(lat, lng, placeLat, placeLng);
                            if (distance < 1000) {
                                result += "\n📏 " + (int)distance + "m away";
                            } else {
                                result += "\n📏 " + String.format("%.1f", distance/1000) + "km away";
                            }
                            
                            if (!category.isEmpty()) {
                                result += "\n🏷️ " + category;
                            }
                            if (rating > 0) {
                                result += "\n⭐ " + rating + " ★";
                            }
                            result += "\n🌐 Google Places";
                            results.add(result);
                        }
                    } else if (status.equals("ZERO_RESULTS")) {
                        Log.d(TAG, "❌ Google: No results found");
                    } else if (status.equals("REQUEST_DENIED")) {
                        Log.e(TAG, "❌ REQUEST_DENIED! API key issue.");
                        results.add("❌ Google Places: API key not authorized. Enable Places API in Google Cloud.");
                    } else {
                        Log.e(TAG, "❌ Google status: " + status);
                        results.add("❌ Google Places: " + status);
                    }
                } else {
                    Log.e(TAG, "❌ HTTP Error: " + responseCode);
                }
                conn.disconnect();
                
                if (results.isEmpty()) {
                    Log.d(TAG, "🔄 Falling back to OpenStreetMap...");
                    results.addAll(searchOpenStreetMap(lat, lng, query, radius));
                }
                
                if (results.isEmpty()) {
                    results.add("❌ No places found. Try different search.");
                    results.add("💡 Try: restaurant, cafe, hospital, bank, KFC");
                }
                
                results.add(0, "✅ " + results.size() + " results found");
                callback.onSuccess(results);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error: " + e.getMessage(), e);
                results.add("❌ Search error: " + e.getMessage());
                callback.onSuccess(results);
            }
        });
    }
    
    private List<String> searchOpenStreetMap(double lat, double lng, String query, int radius) {
        List<String> results = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            
            double latOffset = Math.max(0.05, (double) radius / 111000.0);
            double lngOffset = Math.max(0.05, (double) radius / (111000.0 * Math.cos(Math.toRadians(lat))));
            
            String urlString = String.format(
                "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=20&addressdetails=1&viewbox=%f,%f,%f,%f&bounded=1",
                encodedQuery, lng - lngOffset, lat - latOffset, lng + lngOffset, lat + latOffset
            );
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "NearWork/1.0");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            if (conn.getResponseCode() == 200) {
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
                
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject place = jsonArray.getJSONObject(i);
                    
                    String displayName = place.optString("display_name", "");
                    String type = place.optString("type", "");
                    double placeLat = place.optDouble("lat", 0);
                    double placeLng = place.optDouble("lon", 0);
                    double distance = calculateDistance(lat, lng, placeLat, placeLng);
                    
                    if (distance > radius) continue;
                    
                    String name = displayName.split(",")[0].trim();
                    if (name.isEmpty()) continue;
                    
                    String result = "🏠 " + name;
                    
                    if (distance < 1000) {
                        result += "\n📏 " + (int)distance + "m away";
                    } else {
                        result += "\n📏 " + String.format("%.1f", distance/1000) + "km away";
                    }
                    if (!type.isEmpty() && !type.equals("amenity")) {
                        result += "\n🏷️ " + type.replace("_", " ");
                    }
                    results.add(result);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "OSM error: " + e.getMessage());
        }
        return results;
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
