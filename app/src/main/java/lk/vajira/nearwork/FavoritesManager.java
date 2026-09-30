package lk.vajira.nearwork;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String PREF_NAME = "NearWorkPrefs";
    private static final String KEY_FAVORITES = "favorites";
    private static FavoritesManager instance;
    private SharedPreferences prefs;
    
    private FavoritesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized FavoritesManager getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesManager(context);
        }
        return instance;
    }
    
    public void addFavorite(String job) {
        Set<String> favorites = getFavorites();
        favorites.add(job);
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }
    
    public void removeFavorite(String job) {
        Set<String> favorites = getFavorites();
        favorites.remove(job);
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }
    
    public boolean isFavorite(String job) {
        return getFavorites().contains(job);
    }
    
    public Set<String> getFavorites() {
        return prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
    }
    
    public void clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply();
    }
}
