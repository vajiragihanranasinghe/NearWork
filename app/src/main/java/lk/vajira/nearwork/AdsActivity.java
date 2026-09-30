package lk.vajira.nearwork;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdsActivity extends AppCompatActivity {

    RecyclerView adsRecyclerView;
    Spinner categorySpinner, typeSpinner, tradeFilterSpinner;
    EditText searchInput;
    Button filterButton, clearSearchButton;
    List<AdModel> adsList = new ArrayList<>();
    AdsAdapter adapter;
    AdManager adManager;
    double currentLat, currentLng;
    String searchQuery = "";
    private final Set<String> blockedUids = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);

        currentLat = getIntent().getDoubleExtra("lat", 6.9271);
        currentLng = getIntent().getDoubleExtra("lng", 79.8612);

        adManager = AdManager.getInstance(this);

        BlockManager.getBlockedUsers(new BlockManager.BlockListCallback() {
            @Override public void onResult(List<String> uids) {
                blockedUids.clear();
                if (uids != null) blockedUids.addAll(uids);
                loadAds();
            }
            @Override public void onError(String message) { }
        });

        adsRecyclerView = findViewById(R.id.adsRecyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        typeSpinner = findViewById(R.id.typeSpinner);
        tradeFilterSpinner = findViewById(R.id.tradeFilterSpinner);
        searchInput = findViewById(R.id.searchInput);
        filterButton = findViewById(R.id.filterButton);
        clearSearchButton = findViewById(R.id.clearSearchButton);
        adsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String[] tradeTypes = {"All Types", "Buy", "Sell", "Exchange/Swap", "Rent", "Free", "Request"};
        ArrayAdapter<String> tradeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, tradeTypes);
        tradeFilterSpinner.setAdapter(tradeAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, adManager.getCategories());
        categorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, adManager.getAdTypes());
        typeSpinner.setAdapter(typeAdapter);

        loadAds();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                loadAds();
            }
        });

        filterButton.setOnClickListener(v -> loadAds());
        clearSearchButton.setOnClickListener(v -> {
            searchInput.setText("");
            searchQuery = "";
            loadAds();
        });
    }

    private void loadAds() {
        adsList.clear();

        String category = categorySpinner.getSelectedItem().toString();
        String type = typeSpinner.getSelectedItem().toString();
        String tradeFilter = tradeFilterSpinner.getSelectedItem().toString();

        List<AdModel> allAds = adManager.getAllAds();

        for (AdModel ad : allAds) {
            boolean matches = true;

            if (!category.equals("All") && !ad.getCategory().equals(category)) matches = false;
            if (matches && !type.equals("All") && !ad.getAdType().equals(type)) matches = false;
            if (matches && !tradeFilter.equals("All Types")) {
                String tradeDisplay = getTradeDisplay(ad.getTradeType());
                if (!tradeDisplay.equals(tradeFilter)) matches = false;
            }

            if (matches && !searchQuery.isEmpty()) {
                String lowerQuery = searchQuery.toLowerCase();
                boolean titleMatch = ad.getTitle() != null && ad.getTitle().toLowerCase().contains(lowerQuery);
                boolean descMatch = ad.getDescription() != null && ad.getDescription().toLowerCase().contains(lowerQuery);
                boolean catMatch = ad.getCategory() != null && ad.getCategory().toLowerCase().contains(lowerQuery);
                boolean locMatch = ad.getLocation() != null && ad.getLocation().toLowerCase().contains(lowerQuery);
                if (!titleMatch && !descMatch && !catMatch && !locMatch) matches = false;
            }

            if (matches && !blockedUids.contains(ad.getUserId())) adsList.add(ad);
        }

        if (adsList.isEmpty()) {
            Toast.makeText(this, "No ads found. Try different search.", Toast.LENGTH_SHORT).show();
        }

        adapter = new AdsAdapter(adsList, this);
        adsRecyclerView.setAdapter(adapter);
    }

    private String getTradeDisplay(String tradeType) {
        if (tradeType == null) return "Sell";
        if (tradeType.contains("Buy")) return "Buy";
        if (tradeType.contains("Sell")) return "Sell";
        if (tradeType.contains("Exchange") || tradeType.contains("Swap")) return "Exchange/Swap";
        if (tradeType.contains("Rent")) return "Rent";
        if (tradeType.contains("Free")) return "Free";
        if (tradeType.contains("Request")) return "Request";
        return "Sell";
    }

    class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.ViewHolder> {
        List<AdModel> ads;
        Context context;

        AdsAdapter(List<AdModel> ads, Context context) {
            this.ads = ads;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_ad_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AdModel ad = ads.get(position);

            String tradeIcon = getTradeIcon(ad.getTradeType());
            String typeIcon = getTypeIcon(ad.getAdType());

            holder.titleText.setText(typeIcon + " " + ad.getTitle());
            holder.tradeTypeText.setText(tradeIcon + " " + ad.getTradeType());
            holder.categoryText.setText(ad.getCategory());
            holder.descriptionText.setText(ad.getDescription());
            holder.priceText.setText(ad.getPrice());
            holder.locationText.setText(ad.getLocation());
            holder.conditionText.setText(ad.getCondition());
            holder.viewsText.setText(ad.getViews() + " views");

            if (ad.hasImages()) {
                holder.imagePreview.setVisibility(View.VISIBLE);
                try {
                    String base64Image = ad.getImages().get(0);
                    Bitmap bitmap = ImageUploadHelper.decodeImageFromBase64(base64Image);
                    if (bitmap != null) {
                        holder.imagePreview.setImageBitmap(bitmap);
                    } else {
                        holder.imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    holder.imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.imagePreview.setVisibility(View.GONE);
            }

            if (ad.getExchangeFor() != null && !ad.getExchangeFor().isEmpty()) {
                holder.exchangeText.setVisibility(View.VISIBLE);
                holder.exchangeText.setText("Wants: " + ad.getExchangeFor());
            } else {
                holder.exchangeText.setVisibility(View.GONE);
            }

            if (ad.isVerified()) {
                holder.verifiedText.setVisibility(View.VISIBLE);
                holder.verifiedText.setText("Verified");
            } else {
                holder.verifiedText.setVisibility(View.GONE);
            }

            if (ad.isFeatured()) {
                holder.featuredText.setVisibility(View.VISIBLE);
                holder.featuredText.setText("FEATURED");
            } else {
                holder.featuredText.setVisibility(View.GONE);
            }

            try {
                long timestamp = Long.parseLong(ad.getTimestamp());
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                holder.dateText.setText(sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                holder.dateText.setText("");
            }

            holder.contactText.setText(ad.getContactType() + ": " + ad.getContact());

            holder.contactButton.setOnClickListener(v -> {
                String contact = ad.getContact();
                String type = ad.getContactType();

                if (type.equals("Phone") || type.equals("WhatsApp") || type.equals("Telegram") || type.equals("Signal")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contact));
                    startActivity(intent);
                } else if (type.equals("Email")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + contact));
                    startActivity(intent);
                }
            });

            holder.shareButton.setOnClickListener(v -> {
                String shareText = ad.getTradeType() + ": " + ad.getTitle() + "\n" +
                                   ad.getCategory() + "\n" +
                                   ad.getPrice() + "\n" +
                                   ad.getLocation() + "\n" +
                                   ad.getContact() + "\n\n" +
                                   "Check it out on NearWork!";
                if (ad.getExchangeFor() != null && !ad.getExchangeFor().isEmpty()) {
                    shareText += "\nWants: " + ad.getExchangeFor();
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            });

            String ownerUid = ad.getUserId();
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUid = fbUser != null ? fbUser.getUid() : null;
            boolean isOwnAd = ownerUid != null && ownerUid.equals(currentUid);

            if (isOwnAd || ownerUid == null) {
                holder.overflowButton.setVisibility(View.GONE);
            } else {
                holder.overflowButton.setVisibility(View.VISIBLE);
                holder.overflowButton.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, holder.overflowButton);
                    popup.getMenu().add(Menu.NONE, 1, 1, "Report ad");
                    popup.getMenu().add(Menu.NONE, 2, 2, "Block user");
                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 1) {
                            ReportDialog.show(context, "ad", ad.getId());
                            return true;
                        } else if (item.getItemId() == 2) {
                            String displayName = ad.getUserName() != null && !ad.getUserName().isEmpty()
                                    ? ad.getUserName() : "this user";
                            ReportDialog.confirmBlock(context, ownerUid, displayName);
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }

            holder.itemView.setOnClickListener(v -> {
                AdModel clickedAd = ads.get(position);
                Toast.makeText(context, clickedAd.getTitle(), Toast.LENGTH_SHORT).show();
                AdModel updatedAd = adManager.getAdById(clickedAd.getId());
                if (updatedAd != null) {
                    Toast.makeText(context, "Views: " + updatedAd.getViews(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private String getTradeIcon(String tradeType) {
            if (tradeType == null) return "";
            if (tradeType.contains("Buy")) return "";
            if (tradeType.contains("Sell")) return "";
            if (tradeType.contains("Exchange") || tradeType.contains("Swap")) return "";
            if (tradeType.contains("Rent")) return "";
            if (tradeType.contains("Free")) return "";
            if (tradeType.contains("Request")) return "";
            return "";
        }

        private String getTypeIcon(String type) {
            if (type == null) return "";
            switch (type) {
                case "Service": return "";
                case "Vehicle": return "";
                case "Item": return "";
                case "Classified": return "";
                default: return "";
            }
        }

        @Override
        public int getItemCount() {
            return ads.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText, tradeTypeText, categoryText, descriptionText, priceText,
                     locationText, conditionText, contactText, viewsText, featuredText,
                     dateText, verifiedText, exchangeText;
            ImageView imagePreview;
            Button contactButton, shareButton;
            ImageButton overflowButton;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.adTitle);
                tradeTypeText = itemView.findViewById(R.id.adTradeType);
                categoryText = itemView.findViewById(R.id.adCategory);
                descriptionText = itemView.findViewById(R.id.adDescription);
                priceText = itemView.findViewById(R.id.adPrice);
                locationText = itemView.findViewById(R.id.adLocation);
                conditionText = itemView.findViewById(R.id.adCondition);
                contactText = itemView.findViewById(R.id.adContact);
                viewsText = itemView.findViewById(R.id.adViews);
                featuredText = itemView.findViewById(R.id.adFeatured);
                dateText = itemView.findViewById(R.id.adDate);
                verifiedText = itemView.findViewById(R.id.adVerified);
                exchangeText = itemView.findViewById(R.id.adExchange);
                imagePreview = itemView.findViewById(R.id.adImagePreview);
                contactButton = itemView.findViewById(R.id.contactButton);
                shareButton = itemView.findViewById(R.id.shareButton);
                overflowButton = itemView.findViewById(R.id.adOverflowButton);
            }
        }
    }
}
