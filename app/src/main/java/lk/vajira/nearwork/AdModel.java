package lk.vajira.nearwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdModel implements Serializable {
    private String id;
    private String title;
    private String description;
    private String category;
    private String subCategory;
    private String price;
    private String tradeType;
    private String contact;
    private String contactType;
    private double latitude;
    private double longitude;
    private String userId;
    private String timestamp;
    private List<String> images; // Base64 encoded images
    private boolean isActive;
    private String condition;
    private String location;
    private String adType;
    private int views;
    private String userPhone;
    private String userName;
    private boolean isFeatured;
    private boolean isVerified;
    private String exchangeFor;

    public AdModel() {
        this.images = new ArrayList<>();
        this.tradeType = "Sell";
    }

    public AdModel(String title, String description, String category, String price, 
                   String contact, String contactType, double lat, double lng) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.contact = contact;
        this.contactType = contactType;
        this.latitude = lat;
        this.longitude = lng;
        this.isActive = true;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.condition = "Good";
        this.location = "Sri Lanka";
        this.views = 0;
        this.isFeatured = false;
        this.isVerified = false;
        this.images = new ArrayList<>();
        this.tradeType = "Sell";
        this.exchangeFor = "";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    
    public String getTradeType() { return tradeType; }
    public void setTradeType(String tradeType) { this.tradeType = tradeType; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public String getContactType() { return contactType; }
    public void setContactType(String contactType) { this.contactType = contactType; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public void addImage(String image) { this.images.add(image); }
    public void addImages(List<String> images) { this.images.addAll(images); }
    public boolean hasImages() { return images != null && !images.isEmpty(); }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getAdType() { return adType; }
    public void setAdType(String adType) { this.adType = adType; }
    
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public void incrementViews() { this.views++; }
    
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    
    public String getExchangeFor() { return exchangeFor; }
    public void setExchangeFor(String exchangeFor) { this.exchangeFor = exchangeFor; }
}
