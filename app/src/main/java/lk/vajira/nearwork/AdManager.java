package lk.vajira.nearwork;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdManager {
    private static final String PREF_NAME = "AdsPrefs";
    private static final String KEY_ADS = "ads_list";
    private static final String KEY_FAVORITES = "favorites";
    private static AdManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    private List<AdModel> ads;
    private List<String> favorites;

    private AdManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadAds();
        loadFavorites();
    }

    public static synchronized AdManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdManager(context);
        }
        return instance;
    }

    private void loadAds() {
        String json = prefs.getString(KEY_ADS, "");
        if (json.isEmpty()) {
            ads = new ArrayList<>();
            addSampleAds();
        } else {
            Type type = new TypeToken<List<AdModel>>() {}.getType();
            ads = gson.fromJson(json, type);
            if (ads == null) {
                ads = new ArrayList<>();
                addSampleAds();
            }
        }
    }

    private void loadFavorites() {
        String json = prefs.getString(KEY_FAVORITES, "");
        if (json.isEmpty()) {
            favorites = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<String>>() {}.getType();
            favorites = gson.fromJson(json, type);
            if (favorites == null) favorites = new ArrayList<>();
        }
    }

    private void saveAds() {
        String json = gson.toJson(ads);
        prefs.edit().putString(KEY_ADS, json).apply();
    }

    private void saveFavorites() {
        String json = gson.toJson(favorites);
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }

    private void addSampleAds() {
        ads.clear();
        
        String[] titles = {
            "🏠 2BR Apartment for Rent - Colombo 07",
            "🚗 Toyota Corolla 2018 - Excellent Condition",
            "🛠️ Professional Plumbing Services",
            "📱 iPhone 14 Pro Max - Like New",
            "👕 Designer Clothes - Brand New",
            "🔧 Electrician Services - Emergency Available",
            "🚗 Honda Civic 2020 - Low Mileage",
            "📚 Books Collection - Fiction & Non-Fiction"
        };
        
        String[] descriptions = {
            "Fully furnished apartment in prime location. 2 bedrooms, 2 bathrooms, parking, security.",
            "Full service history, 50,000 km, well maintained. AC, power steering, ABS.",
            "10 years experience. All plumbing work. Emergency service available.",
            "256GB, battery health 95%, original box and accessories included.",
            "Various designer brands. New condition. Free delivery in Colombo.",
            "Licensed electrician. All electrical work. Fast and reliable.",
            "60,000 km, full service history, sunroof, leather seats.",
            "Mixed collection of fiction and non-fiction books. Great condition."
        };
        
        String[] categories = {
            "Classified > Housing",
            "Vehicle > Car",
            "Service > Plumbing",
            "Item > Electronics",
            "Item > Clothing",
            "Service > Electrician",
            "Vehicle > Car",
            "Item > Books"
        };
        
        String[] prices = {
            "LKR 150,000/month",
            "LKR 4,500,000",
            "From LKR 2,000",
            "LKR 250,000",
            "LKR 15,000",
            "From LKR 1,500",
            "LKR 6,500,000",
            "LKR 5,000"
        };
        
        String[] types = {"Classified", "Vehicle", "Service", "Item", "Item", "Service", "Vehicle", "Item"};
        String[] conditions = {"Excellent", "Excellent", "Good", "Like New", "New", "Good", "Excellent", "Good"};
        String[] contacts = {"+94 77 123 4567", "+94 71 234 5678", "+94 76 345 6789", 
                            "+94 72 345 6789", "+94 78 456 7890", "+94 77 987 6543",
                            "+94 70 123 4567", "+94 75 234 5678"};
        String[] locations = {"Colombo", "Colombo", "Colombo", "Kandy", "Colombo", "Colombo", "Negombo", "Galle"};
        
        for (int i = 0; i < titles.length; i++) {
            AdModel ad = new AdModel(
                titles[i], descriptions[i], categories[i], prices[i],
                contacts[i], "Phone",
                6.9271 + (Math.random() - 0.5) * 0.1,
                79.8612 + (Math.random() - 0.5) * 0.1
            );
            ad.setId(String.valueOf(System.currentTimeMillis() + i));
            ad.setCondition(conditions[i]);
            ad.setLocation(locations[i]);
            ad.setAdType(types[i]);
            ad.setViews((int)(Math.random() * 100));
            ad.setFeatured(i < 2);
            ads.add(ad);
        }
        
        saveAds();
    }

    public List<AdModel> getAllAds() {
        return new ArrayList<>(ads);
    }

    public List<AdModel> getFeaturedAds() {
        List<AdModel> result = new ArrayList<>();
        for (AdModel ad : ads) {
            if (ad.isFeatured()) result.add(ad);
        }
        return result;
    }

    public List<AdModel> searchAds(String query) {
        List<AdModel> result = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        
        for (AdModel ad : ads) {
            if ((ad.getTitle() != null && ad.getTitle().toLowerCase().contains(lowerQuery)) ||
                (ad.getDescription() != null && ad.getDescription().toLowerCase().contains(lowerQuery)) ||
                (ad.getCategory() != null && ad.getCategory().toLowerCase().contains(lowerQuery)) ||
                (ad.getLocation() != null && ad.getLocation().toLowerCase().contains(lowerQuery)) ||
                (ad.getAdType() != null && ad.getAdType().toLowerCase().contains(lowerQuery))) {
                result.add(ad);
            }
        }
        return result;
    }

    public List<AdModel> getAdsByType(String type) {
        List<AdModel> result = new ArrayList<>();
        for (AdModel ad : ads) {
            if (type.equals("All") || (ad.getAdType() != null && ad.getAdType().equals(type))) {
                result.add(ad);
            }
        }
        return result;
    }

    public List<AdModel> getAdsByCategory(String category) {
        List<AdModel> result = new ArrayList<>();
        for (AdModel ad : ads) {
            if (category.equals("All") || (ad.getCategory() != null && ad.getCategory().equals(category))) {
                result.add(ad);
            }
        }
        return result;
    }

    public List<AdModel> getNearbyAds(double lat, double lng, int radiusMeters) {
        List<AdModel> result = new ArrayList<>();
        for (AdModel ad : ads) {
            double distance = calculateDistance(lat, lng, ad.getLatitude(), ad.getLongitude());
            if (distance <= radiusMeters) {
                result.add(ad);
            }
        }
        return result;
    }

    public AdModel getAdById(String id) {
        for (AdModel ad : ads) {
            if (ad.getId().equals(id)) {
                ad.incrementViews();
                saveAds();
                return ad;
            }
        }
        return null;
    }

    public void addAd(AdModel ad) {
        ad.setId(String.valueOf(System.currentTimeMillis()));
        ad.setViews(0);
        ads.add(0, ad);
        saveAds();
    }

    public void deleteAd(String id) {
        ads.removeIf(ad -> ad.getId().equals(id));
        saveAds();
    }

    public void addFavorite(String adId) {
        if (!favorites.contains(adId)) {
            favorites.add(adId);
            saveFavorites();
        }
    }

    public void removeFavorite(String adId) {
        favorites.remove(adId);
        saveFavorites();
    }

    public boolean isFavorite(String adId) {
        return favorites.contains(adId);
    }

    public List<AdModel> getFavorites() {
        List<AdModel> result = new ArrayList<>();
        for (String id : favorites) {
            AdModel ad = getAdById(id);
            if (ad != null) result.add(ad);
        }
        return result;
    }

    public List<AdModel> getLatestAds(int limit) {
        List<AdModel> sorted = new ArrayList<>(ads);
        Collections.sort(sorted, (a, b) -> {
            try {
                long timeA = Long.parseLong(a.getTimestamp());
                long timeB = Long.parseLong(b.getTimestamp());
                return Long.compare(timeB, timeA);
            } catch (Exception e) {
                return 0;
            }
        });
        return sorted.size() > limit ? sorted.subList(0, limit) : sorted;
    }

    public List<AdModel> getPopularAds(int limit) {
        List<AdModel> sorted = new ArrayList<>(ads);
        Collections.sort(sorted, (a, b) -> Integer.compare(b.getViews(), a.getViews()));
        return sorted.size() > limit ? sorted.subList(0, limit) : sorted;
    }

    public List<AdModel> getAdsByUser(String userId) {
        List<AdModel> result = new ArrayList<>();
        for (AdModel ad : ads) {
            if (ad.getUserId() != null && ad.getUserId().equals(userId)) {
                result.add(ad);
            }
        }
        return result;
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

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Classified > Housing");
        categories.add("Classified > Jobs");
        categories.add("Classified > Services");
        categories.add("Vehicle > Car");
        categories.add("Vehicle > Motorcycle");
        categories.add("Vehicle > Bicycle");
        categories.add("Service > Plumbing");
        categories.add("Service > Electrician");
        categories.add("Service > Cleaning");
        categories.add("Service > Tutoring");
        categories.add("Item > Electronics");
        categories.add("Item > Furniture");
        categories.add("Item > Appliances");
        categories.add("Item > Clothing");
        categories.add("Item > Books");
        return categories;
    }

    public List<String> getAdTypes() {
        List<String> types = new ArrayList<>();
        types.add("All");
        types.add("Service");
        types.add("Vehicle");
        types.add("Item");
        types.add("Classified");
        return types;
    }
}
