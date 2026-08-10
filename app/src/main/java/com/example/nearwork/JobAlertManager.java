package com.example.nearwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobAlertManager {
    
    private static final String PREF_NAME = "JobAlerts";
    private static final String KEY_ALERTS = "job_alerts";
    private static final String KEY_HISTORY = "search_history";
    private static JobAlertManager instance;
    private SharedPreferences prefs;
    private Context context;
    
    private JobAlertManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized JobAlertManager getInstance(Context context) {
        if (instance == null) {
            instance = new JobAlertManager(context);
        }
        return instance;
    }
    
    // Save job search as alert
    public void saveJobAlert(String searchQuery, String location, String radius) {
        String alert = searchQuery + "|" + location + "|" + radius;
        Set<String> alerts = getAlerts();
        alerts.add(alert);
        prefs.edit().putStringSet(KEY_ALERTS, alerts).apply();
        
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, "🔔 Job alert saved for: " + searchQuery, Toast.LENGTH_LONG).show()
        );
    }
    
    // Get all saved alerts
    public Set<String> getAlerts() {
        return prefs.getStringSet(KEY_ALERTS, new HashSet<>());
    }
    
    // Get alerts as list
    public List<String> getAlertList() {
        return new ArrayList<>(getAlerts());
    }
    
    // Remove alert
    public void removeAlert(String alert) {
        Set<String> alerts = getAlerts();
        alerts.remove(alert);
        prefs.edit().putStringSet(KEY_ALERTS, alerts).apply();
    }
    
    // Check if alert exists
    public boolean hasAlert(String searchQuery) {
        for (String alert : getAlerts()) {
            if (alert.startsWith(searchQuery + "|")) {
                return true;
            }
        }
        return false;
    }
    
    // Clear all alerts
    public void clearAllAlerts() {
        prefs.edit().remove(KEY_ALERTS).apply();
    }
    
    // Add to search history
    public void addToHistory(String query) {
        Set<String> history = getHistory();
        history.add(query);
        prefs.edit().putStringSet(KEY_HISTORY, history).apply();
    }
    
    // Get search history
    public Set<String> getHistory() {
        return prefs.getStringSet(KEY_HISTORY, new HashSet<>());
    }
    
    // Clear history
    public void clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply();
    }
}
