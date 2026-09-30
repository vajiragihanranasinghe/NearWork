package lk.vajira.nearwork;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyAdsActivity extends AppCompatActivity {

    RecyclerView myAdsRecyclerView;
    TextView emptyText;
    List<AdModel> myAds = new ArrayList<>();
    MyAdsAdapter adapter;
    AdManager adManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ads);

        myAdsRecyclerView = findViewById(R.id.myAdsRecyclerView);
        emptyText = findViewById(R.id.emptyText);

        myAdsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adManager = AdManager.getInstance(this);

        loadMyAds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyAds();
    }

    private void loadMyAds() {
        myAds.clear();
        // Get ads by current user
        UserManager userManager = UserManager.getInstance(this);
        String userId = userManager.getUserId();
        
        for (AdModel ad : adManager.getAllAds()) {
            if (ad.getUserId() != null && ad.getUserId().equals(userId)) {
                myAds.add(ad);
            }
        }

        if (myAds.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            myAdsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            myAdsRecyclerView.setVisibility(View.VISIBLE);
            adapter = new MyAdsAdapter(myAds, this);
            myAdsRecyclerView.setAdapter(adapter);
        }
    }

    class MyAdsAdapter extends RecyclerView.Adapter<MyAdsAdapter.ViewHolder> {
        List<AdModel> ads;
        Context context;

        MyAdsAdapter(List<AdModel> ads, Context context) {
            this.ads = ads;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_my_ad, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AdModel ad = ads.get(position);
            
            String tradeIcon = getTradeIcon(ad.getTradeType());
            holder.titleText.setText(tradeIcon + " " + ad.getTitle());
            holder.categoryText.setText("📌 " + ad.getCategory());
            holder.priceText.setText("💰 " + ad.getPrice());
            holder.locationText.setText("📍 " + ad.getLocation());
            holder.viewsText.setText("👁️ " + ad.getViews() + " views");
            
            // Date
            try {
                long timestamp = Long.parseLong(ad.getTimestamp());
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                holder.dateText.setText("📅 " + sdf.format(new Date(timestamp)));
            } catch (Exception e) {
                holder.dateText.setText("");
            }

            // Edit button
            holder.editButton.setOnClickListener(v -> {
                Toast.makeText(context, "✏️ Edit: " + ad.getTitle(), Toast.LENGTH_SHORT).show();
                // Open edit activity
                Intent intent = new Intent(context, PostAdActivity.class);
                intent.putExtra("edit_mode", true);
                intent.putExtra("ad_id", ad.getId());
                intent.putExtra("lat", ad.getLatitude());
                intent.putExtra("lng", ad.getLongitude());
                context.startActivity(intent);
            });

            // Delete button
            holder.deleteButton.setOnClickListener(v -> {
                adManager.deleteAd(ad.getId());
                Toast.makeText(context, "🗑️ Ad deleted: " + ad.getTitle(), Toast.LENGTH_SHORT).show();
                loadMyAds();
            });
        }

        private String getTradeIcon(String tradeType) {
            if (tradeType == null) return "💰";
            if (tradeType.contains("Buy")) return "🛒";
            if (tradeType.contains("Sell")) return "💰";
            if (tradeType.contains("Exchange") || tradeType.contains("Swap")) return "🔄";
            if (tradeType.contains("Rent")) return "📋";
            if (tradeType.contains("Free")) return "🎁";
            if (tradeType.contains("Request")) return "🙏";
            return "💰";
        }

        @Override
        public int getItemCount() {
            return ads.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText, categoryText, priceText, locationText, viewsText, dateText;
            Button editButton, deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.myAdTitle);
                categoryText = itemView.findViewById(R.id.myAdCategory);
                priceText = itemView.findViewById(R.id.myAdPrice);
                locationText = itemView.findViewById(R.id.myAdLocation);
                viewsText = itemView.findViewById(R.id.myAdViews);
                dateText = itemView.findViewById(R.id.myAdDate);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
