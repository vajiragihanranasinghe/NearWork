package lk.vajira.nearwork;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public class LanguageHelper {
    
    private static final String PREF_NAME = "LanguagePrefs";
    private static final String KEY_LANGUAGE = "language_code";
    private static final String DEFAULT_LANGUAGE = "en";
    
    private static LanguageHelper instance;
    private SharedPreferences prefs;
    private String currentLanguage = DEFAULT_LANGUAGE;
    private Map<String, Map<String, String>> translations;
    
    private LanguageHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentLanguage = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
        initTranslations();
    }
    
    public static synchronized LanguageHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LanguageHelper(context);
        }
        return instance;
    }
    
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }
    
    public void setLanguage(String langCode) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
        currentLanguage = langCode;
    }
    
    public String get(String key) {
        Map<String, String> langMap = translations.get(currentLanguage);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        Map<String, String> enMap = translations.get("en");
        if (enMap != null && enMap.containsKey(key)) {
            return enMap.get(key);
        }
        return key;
    }
    
    public String[] getLanguages() {
        return new String[]{"🇬🇧 English", "🇱🇰 Sinhala", "🇱🇰 Tamil", "🇮🇳 Hindi", "🇸🇦 Arabic"};
    }
    
    public String[] getLanguageCodes() {
        return new String[]{"en", "si", "ta", "hi", "ar"};
    }
    
    private void initTranslations() {
        translations = new HashMap<>();
        
        // ENGLISH
        Map<String, String> en = new HashMap<>();
        en.put("search", "Search");
        en.put("search_here", "🔎 What do you need?");
        en.put("search_nearby", "🔍 Search Nearby");
        en.put("results", "📋 Results");
        en.put("no_results", "🔍 Search to find places");
        en.put("get_my_location", "📍 Get My Location");
        en.put("pick_on_map", "🗺️ Pick on Map");
        en.put("home", "Home");
        en.put("favorites", "Favorites");
        en.put("my_ads", "My Ads");
        en.put("profile", "Profile");
        en.put("settings", "Settings");
        translations.put("en", en);
        
        // SINHALA
        Map<String, String> si = new HashMap<>();
        si.put("search", "සොයන්න");
        si.put("search_here", "🔎 ඔබට අවශ්‍ය කුමක්ද?");
        si.put("search_nearby", "🔍 අසල සොයන්න");
        si.put("results", "📋 ප්‍රතිඵල");
        si.put("no_results", "🔍 ස්ථාන සොයා ගැනීමට සෙවීම කරන්න");
        si.put("get_my_location", "📍 මගේ ස්ථානය ලබා ගන්න");
        si.put("pick_on_map", "🗺️ සිතියමෙන් තෝරන්න");
        si.put("home", "මුල් පිටුව");
        si.put("favorites", "ප්‍රියතම");
        si.put("my_ads", "මගේ දැන්වීම්");
        si.put("profile", "පැතිකඩ");
        si.put("settings", "සැකසුම්");
        translations.put("si", si);
        
        // TAMIL
        Map<String, String> ta = new HashMap<>();
        ta.put("search", "தேடு");
        ta.put("search_here", "🔎 உங்களுக்கு என்ன தேவை?");
        ta.put("search_nearby", "🔍 அருகில் தேடுக");
        ta.put("results", "📋 முடிவுகள்");
        ta.put("no_results", "🔍 இடங்களை கண்டுபிடிக்க தேடவும்");
        ta.put("get_my_location", "📍 என் இருப்பிடத்தை பெறுக");
        ta.put("pick_on_map", "🗺️ வரைபடத்தில் தேர்ந்தெடுக்கவும்");
        ta.put("home", "முகப்பு");
        ta.put("favorites", "பிடித்தவை");
        ta.put("my_ads", "என் விளம்பரங்கள்");
        ta.put("profile", "சுயவிவரம்");
        ta.put("settings", "அமைப்புகள்");
        translations.put("ta", ta);
        
        // HINDI
        Map<String, String> hi = new HashMap<>();
        hi.put("search", "खोजें");
        hi.put("search_here", "🔎 आपको क्या चाहिए?");
        hi.put("search_nearby", "🔍 पास में खोजें");
        hi.put("results", "📋 परिणाम");
        hi.put("no_results", "🔍 स्थान खोजने के लिए खोजें");
        hi.put("get_my_location", "📍 मेरा स्थान प्राप्त करें");
        hi.put("pick_on_map", "🗺️ मानचित्र पर चुनें");
        hi.put("home", "होम");
        hi.put("favorites", "पसंदीदा");
        hi.put("my_ads", "मेरे विज्ञापन");
        hi.put("profile", "प्रोफ़ाइल");
        hi.put("settings", "सेटिंग्स");
        translations.put("hi", hi);
        
        // ARABIC
        Map<String, String> ar = new HashMap<>();
        ar.put("search", "بحث");
        ar.put("search_here", "🔎 ماذا تريد؟");
        ar.put("search_nearby", "🔍 بحث بالقرب");
        ar.put("results", "📋 النتائج");
        ar.put("no_results", "🔍 ابحث للعثور على أماكن");
        ar.put("get_my_location", "📍 احصل على موقعي");
        ar.put("pick_on_map", "🗺️ اختر على الخريطة");
        ar.put("home", "الرئيسية");
        ar.put("favorites", "المفضلة");
        ar.put("my_ads", "إعلاناتي");
        ar.put("profile", "الملف الشخصي");
        ar.put("settings", "الإعدادات");
        translations.put("ar", ar);
    }
}
