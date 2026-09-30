package lk.vajira.nearwork;

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

public class JobApiService {
    
    private static final String TAG = "JobApiService";
    private static JobApiService instance;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static synchronized JobApiService getInstance() {
        if (instance == null) {
            instance = new JobApiService();
        }
        return instance;
    }
    
    public void searchJobs(String query, String location, JobSearchCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String encodedLocation = URLEncoder.encode(location, "UTF-8");
                
                // Try GitHub Jobs API first
                String urlString = String.format(
                    "https://jobs.github.com/positions.json?description=%s&location=%s",
                    encodedQuery, encodedLocation
                );
                
                Log.d(TAG, "Searching jobs: " + urlString);
                
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "NearWork Android App/1.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setUseCaches(false);
                
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Job API response: " + responseCode);
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    List<String> jobs = parseJobs(response.toString());
                    if (!jobs.isEmpty()) {
                        callback.onSuccess(jobs);
                        return;
                    }
                }
                
                // If API fails or returns empty, use mock data
                Log.d(TAG, "Using mock jobs data");
                callback.onSuccess(getMockJobs(query));
                
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
                callback.onSuccess(getMockJobs(query));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
    
    private List<String> parseJobs(String jsonResponse) {
        List<String> jobs = new ArrayList<>();
        try {
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return jobs;
            }
            
            JSONArray jsonArray = new JSONArray(jsonResponse);
            
            for (int i = 0; i < Math.min(jsonArray.length(), 15); i++) {
                JSONObject job = jsonArray.getJSONObject(i);
                
                String title = job.optString("title", "Job Position");
                String company = job.optString("company", "Company");
                String location = job.optString("location", "Location");
                String type = job.optString("type", "Full-time");
                String description = job.optString("description", "");
                
                // Clean description
                if (description.length() > 100) {
                    description = description.substring(0, 100) + "...";
                }
                
                String result = "💼 " + title;
                result += "\n🏢 " + company;
                result += "\n📍 " + location;
                result += "\n📋 " + type;
                if (!description.isEmpty()) {
                    result += "\n📝 " + description;
                }
                
                jobs.add(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error: " + e.getMessage(), e);
        }
        return jobs;
    }
    
    private List<String> getMockJobs(String query) {
        List<String> jobs = new ArrayList<>();
        String[] companies = {"Tech Corp", "Business Inc", "Service Co", "Consulting Ltd", "Office Corp"};
        String[] types = {"Full-time", "Part-time", "Contract", "Remote", "Hybrid"};
        String[] locations = {"Colombo, Sri Lanka", "Kandy, Sri Lanka", "Galle, Sri Lanka", "Negombo, Sri Lanka", "Jaffna, Sri Lanka"};
        
        for (int i = 0; i < 5; i++) {
            String job = "💼 " + query + " " + (i == 0 ? "Developer" : i == 1 ? "Manager" : i == 2 ? "Specialist" : i == 3 ? "Consultant" : "Assistant");
            job += "\n🏢 " + companies[i % companies.length];
            job += "\n📍 " + locations[i % locations.length];
            job += "\n📋 " + types[i % types.length];
            jobs.add(job);
        }
        return jobs;
    }
    
    public interface JobSearchCallback {
        void onSuccess(List<String> jobs);
        void onError(String error);
    }
}
