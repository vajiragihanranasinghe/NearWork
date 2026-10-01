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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdsFeedFragment extends Fragment {

    RecyclerView adsRecyclerView, categoriesRecycler;
    Spinner tradeFilterSpinner, typeSpinner;
    EditText searchInput;
    ImageButton clearSearchButton;
    LinearLayout emptyState;

    List<AdModel> adsList = new ArrayList<>();
    AdsAdapter adapter;
    AdManager adManager;
    String searchQuery = "";
    String selectedCategory = "All";
    final Set<String> blockedUids = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ads_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        adManager = AdManager.getInstance(requireContext());

        adsRecyclerView = v.findViewById(R.id.adsRecyclerView);
        categoriesRecycler = v.findViewById(R.id.categoriesRecycler);
        tradeFilterSpinner = v.findViewById(R.id.tradeFilterSpinner);
        typeSpinner = v.findViewById(R.id.typeSpinner);
        searchInput = v.findViewById(R.id.searchInput);
        clearSearchButton = v.findViewById(R.id.clearSearchButton);
        emptyState = v.findViewById(R.id.emptyState);

        adsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoriesRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Trade filter spinner
        String[] tradeTypes = {"All Types", "Buy", "Sell", "Exchange/Swap", "Rent", "Free", "Request"};
        tradeFilterSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, tradeTypes));

        // Ad type spinner
        typeSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, adManager.getAdTypes()));

        // Category chips
        List<String> cats = new ArrayList<>();
        cats.add("All");
        cats.addAll(adManager.getCategories());
        categoriesRecycler.setAdapter(new CategoryAdapter(cats));

        // Search
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                clearSearchButton.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                loadAds();
            }
        });

        clearSearchButton.setOnClickListener(x -> searchInput.setText(""));

        // Reload every time filter changes
        tradeFilterSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                loadAds();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        typeSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                loadAds();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });

        // Blocked users
        BlockManager.getBlockedUsers(new BlockManager.BlockListCallback() {
            @Override public void onResult(List<String> uids) {
                blockedUids.clear();
                if (uids != null) blockedUids.addAll(uids);
                loadAds();
            }
            @Override public void onError(String message) { loadAds(); }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAds();
    }

    private void loadAds() {
        if (adsRecyclerView == null) return;
        adsList.clear();

        String tradeFilter = tradeFilterSpinner.getSelectedItem() != null
                ? tradeFilterSpinner.getSelectedItem().toString() : "All Types";
        String type = typeSpinner.getSelectedItem() != null
                ? typeSpinner.getSelectedItem().toString() : "All";

        List<AdModel> allAds = adManager.getAllAds();

        for (AdModel ad : allAds) {
            boolean matches = true;

            if (!"All".equals(selectedCategory) &&
                    (ad.getCategory() == null || !ad.getCategory().equals(selectedCategory)))
                matches = false;

            if (matches && !"All".equals(type) &&
                    (ad.getAdType() == null || !ad.getAdType().equals(type)))
                matches = false;

            if (matches && !"All Types".equals(tradeFilter)) {
                if (!getTradeDisplay(ad.getTradeType()).equals(tradeFilter)) matches = false;
            }

            if (matches && !searchQuery.isEmpty()) {
                String q = searchQuery.toLowerCase();
                boolean t = ad.getTitle() != null && ad.getTitle().toLowerCase().contains(q);
                boolean d = ad.getDescription() != null && ad.getDescription().toLowerCase().contains(q);
                boolean c = ad.getCategory() != null && ad.getCategory().toLowerCase().contains(q);
                boolean l = ad.getLocation() != null && ad.getLocation().toLowerCase().contains(q);
                if (!t && !d && !c && !l) matches = false;
            }

            if (matches && !blockedUids.contains(ad.getUserId())) adsList.add(ad);
        }

        // Featured first
        adsList.sort((a, b) -> Boolean.compare(b.isFeatured(), a.isFeatured()));

        adapter = new AdsAdapter(adsList, requireContext());
        adsRecyclerView.setAdapter(adapter);

        if (adsList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            adsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            adsRecyclerView.setVisibility(View.VISIBLE);
        }
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

    // ============ Category chips adapter ============
    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CVH> {
        List<String> items;
        CategoryAdapter(List<String> items) { this.items = items; }

        @NonNull @Override
        public CVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_chip, parent, false);
            return new CVH(v);
        }
        @Override
        public void onBindViewHolder(@NonNull CVH h, int position) {
            String cat = items.get(position);
            h.text.setText(cat);
            boolean sel = cat.equals(selectedCategory);
            h.text.setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
            h.text.setTextColor(getResources().getColor(sel ? R.color.white : R.color.text_secondary, null));
            h.text.setOnClickListener(v -> {
                selectedCategory = cat;
                notifyDataSetChanged();
                loadAds();
            });
        }
        @Override public int getItemCount() { return items.size(); }
        class CVH extends RecyclerView.ViewHolder {
            TextView text;
            CVH(View v) { super(v); text = v.findViewById(R.id.chipText); }
        }
    }

    // ============ Ad card adapter ============
    class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.ViewHolder> {
        List<AdModel> ads;
        Context context;

        AdsAdapter(List<AdModel> ads, Context context) {
            this.ads = ads;
            this.context = context;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_ad_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AdModel ad = ads.get(position);

            holder.titleText.setText(ad.getTitle());
            holder.tradeTypeText.setText(ad.getTradeType());
            holder.categoryText.setText(ad.getCategory());
            holder.descriptionText.setText(ad.getDescription());
            holder.priceText.setText(ad.getPrice());
            holder.locationText.setText("📍 " + (ad.getLocation() != null ? ad.getLocation() : ""));
            holder.viewsText.setText(ad.getViews() + " views");

            if (ad.hasImages()) {
                holder.imagePreview.setVisibility(View.VISIBLE);
                try {
                    Bitmap bm = ImageUploadHelper.decodeImageFromBase64(ad.getImages().get(0));
                    if (bm != null) holder.imagePreview.setImageBitmap(bm);
                    else holder.imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                } catch (Exception e) {
                    holder.imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.imagePreview.setVisibility(View.GONE);
            }

            if (ad.getExchangeFor() != null && !ad.getExchangeFor().isEmpty()) {
                holder.exchangeText.setVisibility(View.VISIBLE);
                holder.exchangeText.setText("🔄 Wants: " + ad.getExchangeFor());
            } else {
                holder.exchangeText.setVisibility(View.GONE);
            }

            holder.verifiedText.setVisibility(ad.isVerified() ? View.VISIBLE : View.GONE);
            holder.featuredText.setVisibility(ad.isFeatured() ? View.VISIBLE : View.GONE);

            holder.contactButton.setOnClickListener(v -> {
                String contact = ad.getContact();
                String type = ad.getContactType();
                if (contact == null) return;
                if ("Phone".equals(type) || "WhatsApp".equals(type) || "Telegram".equals(type) || "Signal".equals(type)) {
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:" + contact));
                    startActivity(i);
                } else if ("Email".equals(type)) {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:" + contact));
                    startActivity(i);
                }
            });

            holder.shareButton.setOnClickListener(v -> {
                String shareText = ad.getTradeType() + ": " + ad.getTitle() + "\n" +
                                   ad.getPrice() + "\n" +
                                   ad.getLocation() + "\n\n" +
                                   "Check it out on NearWork!";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            });

            String ownerUid = ad.getUserId();
            FirebaseUser fb = FirebaseAuth.getInstance().getCurrentUser();
            String currentUid = fb != null ? fb.getUid() : null;
            boolean isOwn = ownerUid != null && ownerUid.equals(currentUid);

            if (isOwn || ownerUid == null) {
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
                            String dn = ad.getUserName() != null && !ad.getUserName().isEmpty()
                                    ? ad.getUserName() : "this user";
                            ReportDialog.confirmBlock(context, ownerUid, dn);
                            return true;
                        }
                        return false;
                    });
                    popup.show();
                });
            }
        }

        @Override public int getItemCount() { return ads.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText, tradeTypeText, categoryText, descriptionText, priceText,
                     locationText, viewsText, featuredText, verifiedText, exchangeText;
            ImageView imagePreview;
            MaterialButton contactButton, shareButton;
            ImageButton overflowButton;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.adTitle);
                tradeTypeText = itemView.findViewById(R.id.adTradeType);
                categoryText = itemView.findViewById(R.id.adCategory);
                descriptionText = itemView.findViewById(R.id.adDescription);
                priceText = itemView.findViewById(R.id.adPrice);
                locationText = itemView.findViewById(R.id.adLocation);
                viewsText = itemView.findViewById(R.id.adViews);
                featuredText = itemView.findViewById(R.id.adFeatured);
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
