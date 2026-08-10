package com.example.nearwork;

import java.util.HashMap;
import java.util.Map;

public class CategoryManager {
    
    public static final String TYPE_SERVICE = "Service";
    public static final String TYPE_VEHICLE = "Vehicle";
    public static final String TYPE_ITEM = "Item";
    public static final String TYPE_CLASSIFIED = "Classified";
    
    // Transaction Types
    public static final String TRANSACTION_BUY = "Buy";
    public static final String TRANSACTION_SELL = "Sell";
    public static final String TRANSACTION_EXCHANGE = "Exchange";
    public static final String TRANSACTION_RENT = "Rent";
    public static final String TRANSACTION_GIVE_AWAY = "Give Away";
    public static final String TRANSACTION_REQUEST = "Request";
    
    public static Map<String, String[]> getCategories() {
        Map<String, String[]> categories = new HashMap<>();
        
        // Service Categories
        categories.put(TYPE_SERVICE, new String[]{
            "Plumbing", "Electrician", "Carpentry", "Painting",
            "Cleaning", "Gardening", "Tutoring", "IT Services",
            "Photography", "Catering", "Event Planning", "Transportation",
            "Pet Care", "Beauty", "Fitness Training", "Accounting",
            "Legal Services", "Medical Services", "Delivery", "Moving",
            "Repair Services", "Consulting", "Web Design", "Content Writing"
        });
        
        // Vehicle Categories
        categories.put(TYPE_VEHICLE, new String[]{
            "Car", "Motorcycle", "Bicycle", "Van",
            "Truck", "Bus", "SUV", "Sedan",
            "Electric Vehicle", "Hybrid", "Classic Car", "Scooter",
            "Boat", "Jet Ski", "ATV", "Tractor"
        });
        
        // Item Categories
        categories.put(TYPE_ITEM, new String[]{
            "Electronics", "Furniture", "Appliances", "Clothing",
            "Books", "Jewelry", "Sports Equipment", "Toys",
            "Tools", "Garden Equipment", "Kitchen Items", "Decor",
            "Music Instruments", "Cameras", "Computers", "Phones",
            "Artwork", "Collectibles", "Antiques", "Handmade Items"
        });
        
        // Classified Categories
        categories.put(TYPE_CLASSIFIED, new String[]{
            "Jobs", "Housing", "Rentals", "Services",
            "Events", "Lost & Found", "Community", "Announcements",
            "Business Opportunities", "Partnerships", "Freelance",
            "Volunteer Opportunities", "Pet Adoption", "Real Estate"
        });
        
        return categories;
    }
    
    public static String[] getTransactionTypes() {
        return new String[]{
            TRANSACTION_SELL,
            TRANSACTION_BUY,
            TRANSACTION_EXCHANGE,
            TRANSACTION_RENT,
            TRANSACTION_GIVE_AWAY,
            TRANSACTION_REQUEST
        };
    }
    
    public static String getTransactionEmoji(String transaction) {
        switch(transaction) {
            case TRANSACTION_SELL: return "💰";
            case TRANSACTION_BUY: return "🛒";
            case TRANSACTION_EXCHANGE: return "🔄";
            case TRANSACTION_RENT: return "📋";
            case TRANSACTION_GIVE_AWAY: return "🎁";
            case TRANSACTION_REQUEST: return "🙏";
            default: return "📌";
        }
    }
    
    public static String getTransactionColor(String transaction) {
        switch(transaction) {
            case TRANSACTION_SELL: return "#48bb78";
            case TRANSACTION_BUY: return "#667eea";
            case TRANSACTION_EXCHANGE: return "#ed8936";
            case TRANSACTION_RENT: return "#9f7aea";
            case TRANSACTION_GIVE_AWAY: return "#fc8181";
            case TRANSACTION_REQUEST: return "#f6ad55";
            default: return "#a0aec0";
        }
    }
}
