package com.example.nearwork;

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

public class JobApiClient {
    
    private static final String TAG = "JobApiClient";
    private static JobApiClient instance;
    private ExecutorService executor = Executors.newFixedThreadPool(3);
    
    // FREE JOB APIS
    private static final String GITHUB_JOBS = "https://jobs.github.com/positions.json";
    private static final String REMOTE_OK = "https://remoteok.com/api";
    private static final String WWR_JOBS = "https://api.workwithus.com/jobs";
    private static final String REACT_JOBS = "https://reactjobs.online/api/jobs";
    
    public static synchronized JobApiClient getInstance() {
        if (instance == null) {
            instance = new JobApiClient();
        }
        return instance;
    }
    
    public void searchJobs(String query, String location, JobSearchCallback callback) {
        executor.execute(() -> {
            List<String> allJobs = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            try {
                // Try GitHub Jobs
                List<String> githubJobs = fetchGitHubJobs(query, location);
                if (!githubJobs.isEmpty()) {
                    allJobs.addAll(githubJobs);
                    Log.d(TAG, "GitHub: Found " + githubJobs.size() + " jobs");
                }
                
                // Try RemoteOK (Remote jobs)
                List<String> remoteJobs = fetchRemoteOKJobs(query);
                if (!remoteJobs.isEmpty()) {
                    allJobs.addAll(remoteJobs);
                    Log.d(TAG, "RemoteOK: Found " + remoteJobs.size() + " jobs");
                }
                
                // Try WWR Jobs
                List<String> wwrJobs = fetchWWRJobs(query);
                if (!wwrJobs.isEmpty()) {
                    allJobs.addAll(wwrJobs);
                    Log.d(TAG, "WWR: Found " + wwrJobs.size() + " jobs");
                }
                
                // If no jobs found from any API, use mock data
                if (allJobs.isEmpty()) {
                    Log.d(TAG, "No jobs from APIs, using mock data");
                    allJobs.addAll(getMockJobs(query));
                }
                
                callback.onSuccess(allJobs);
                
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
                callback.onSuccess(getMockJobs(query));
            }
        });
    }
    
    // GitHub Jobs API
    private List<String> fetchGitHubJobs(String query, String location) {
        List<String> jobs = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            String urlString = String.format(
                "%s?description=%s&location=%s&full_time=true",
                GITHUB_JOBS, encodedQuery, encodedLocation
            );
            
            String response = fetchUrl(urlString);
            if (response != null && !response.isEmpty()) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < Math.min(jsonArray.length(), 10); i++) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    String title = job.optString("title", "Job");
                    String company = job.optString("company", "Company");
                    String loc = job.optString("location", "Location");
                    String type = job.optString("type", "Full-time");
                    
                    String result = "💼 " + title;
                    result += "\n🏢 " + company;
                    result += "\n📍 " + loc;
                    result += "\n📋 " + type;
                    result += "\n🌐 GitHub Jobs";
                    jobs.add(result);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "GitHub error: " + e.getMessage());
        }
        return jobs;
    }
    
    // RemoteOK API (Remote jobs)
    private List<String> fetchRemoteOKJobs(String query) {
        List<String> jobs = new ArrayList<>();
        try {
            String urlString = REMOTE_OK;
            String response = fetchUrl(urlString);
            
            if (response != null && !response.isEmpty()) {
                JSONArray jsonArray = new JSONArray(response);
                // Skip first item (metadata)
                for (int i = 1; i < Math.min(jsonArray.length(), 10); i++) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    String title = job.optString("position", "Remote Job");
                    String company = job.optString("company", "Remote Company");
                    String tags = job.optString("tags", "");
                    
                    // Check if matches query
                    if (title.toLowerCase().contains(query.toLowerCase()) || 
                        tags.toLowerCase().contains(query.toLowerCase())) {
                        String result = "💼 " + title + " 🌍";
                        result += "\n🏢 " + company;
                        result += "\n📍 Remote";
                        result += "\n📋 Remote";
                        result += "\n🌐 RemoteOK";
                        jobs.add(result);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "RemoteOK error: " + e.getMessage());
        }
        return jobs;
    }
    
    // WWR Jobs (Work With Us)
    private List<String> fetchWWRJobs(String query) {
        List<String> jobs = new ArrayList<>();
        try {
            String urlString = WWR_JOBS;
            String response = fetchUrl(urlString);
            
            if (response != null && !response.isEmpty()) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < Math.min(jsonArray.length(), 10); i++) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    String title = job.optString("title", "Job");
                    String company = job.optString("company", "Company");
                    String location = job.optString("location", "Location");
                    
                    if (title.toLowerCase().contains(query.toLowerCase())) {
                        String result = "💼 " + title;
                        result += "\n🏢 " + company;
                        result += "\n📍 " + location;
                        result += "\n📋 Full-time";
                        result += "\n🌐 WWR Jobs";
                        jobs.add(result);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "WWR error: " + e.getMessage());
        }
        return jobs;
    }
    
    // Helper: Fetch URL
    private String fetchUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "NearWork App");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
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
                connection.disconnect();
                return response.toString();
            }
            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Fetch error: " + e.getMessage());
        }
        return null;
    }
    
    // Enhanced mock data with more variety
    private List<String> getMockJobs(String query) {
        List<String> jobs = new ArrayList<>();
        
        String[][] mockData = {
            {"Software Developer", "Tech Corp", "Colombo", "Full-time", "Tech"},
            {"Data Analyst", "DataFlow Inc", "Kandy", "Full-time", "Analytics"},
            {"UX Designer", "Creative Studio", "Galle", "Part-time", "Design"},
            {"Project Manager", "MegaCorp", "Negombo", "Contract", "Management"},
            {"Frontend Developer", "WebWorks", "Jaffna", "Remote", "Tech"},
            {"Backend Engineer", "CloudTech", "Colombo", "Full-time", "Tech"},
            {"DevOps Engineer", "SysAdmin Pro", "Kandy", "Full-time", "Tech"},
            {"Mobile Developer", "AppFactory", "Galle", "Full-time", "Tech"},
            {"QA Engineer", "TestCorp", "Colombo", "Full-time", "Tech"},
            {"Business Analyst", "ConsultPro", "Negombo", "Contract", "Analytics"},
            {"Marketing Specialist", "BrandAgency", "Colombo", "Full-time", "Marketing"},
            {"Sales Representative", "SalesForce", "Kandy", "Full-time", "Sales"},
            {"Customer Support", "HelpDesk", "Galle", "Part-time", "Support"},
            {"Content Writer", "ContentLab", "Negombo", "Remote", "Writing"},
            {"Graphic Designer", "DesignHub", "Colombo", "Full-time", "Design"},
            {"HR Manager", "PeopleCorp", "Kandy", "Full-time", "HR"},
            {"Accountant", "FinancePro", "Galle", "Full-time", "Finance"},
            {"Network Engineer", "NetWorks", "Colombo", "Full-time", "Tech"},
            {"Security Analyst", "SafeNet", "Kandy", "Full-time", "Tech"},
            {"Cloud Architect", "CloudMasters", "Negombo", "Remote", "Tech"}
        };
        
        int count = 0;
        for (String[] job : mockData) {
            if (count >= 15) break;
            String lowerQuery = query.toLowerCase();
            if (lowerQuery.contains("job") || lowerQuery.contains("work") || 
                lowerQuery.contains("employment") || lowerQuery.contains("career") ||
                job[0].toLowerCase().contains(lowerQuery) ||
                job[1].toLowerCase().contains(lowerQuery) ||
                job[4].toLowerCase().contains(lowerQuery)) {
                
                String result = "💼 " + job[0];
                result += "\n🏢 " + job[1];
                result += "\n📍 " + job[2];
                result += "\n📋 " + job[3];
                result += "\n🏷️ " + job[4];
                jobs.add(result);
                count++;
            }
        }
        
        // If no matches, add generic jobs
        if (jobs.isEmpty()) {
            jobs.add("💼 " + query + " Developer\n🏢 Tech Corp\n📍 Colombo\n📋 Full-time\n🏷️ Tech");
            jobs.add("💼 " + query + " Manager\n🏢 Business Inc\n📍 Kandy\n📋 Full-time\n🏷️ Management");
            jobs.add("💼 " + query + " Specialist\n🏢 Service Co\n📍 Galle\n📋 Part-time\n🏷️ Service");
        }
        
        return jobs;
    }
    
    public interface JobSearchCallback {
        void onSuccess(List<String> jobs);
        void onError(String error);
    }
}
