package lk.vajira.nearwork;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.github.dhaval2404.imagepicker.ImagePicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostAdActivity extends AppCompatActivity {

    EditText titleInput, descriptionInput, priceInput, contactInput, locationInput, exchangeInput;
    Spinner categoryTypeSpinner, subCategorySpinner, contactTypeSpinner, conditionSpinner, tradeTypeSpinner;
    Button postButton, addImageButton;
    ImageView selectedImagePreview;
    
    AdManager adManager;
    double currentLat, currentLng;
    Map<String, String[]> categories;
    List<Bitmap> selectedImages = new ArrayList<>();
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        currentLat = getIntent().getDoubleExtra("lat", 6.9271);
        currentLng = getIntent().getDoubleExtra("lng", 79.8612);

        adManager = AdManager.getInstance(this);
        categories = CategoryManager.getCategories();

        // Initialize views
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        priceInput = findViewById(R.id.priceInput);
        contactInput = findViewById(R.id.contactInput);
        locationInput = findViewById(R.id.locationInput);
        exchangeInput = findViewById(R.id.exchangeInput);
        categoryTypeSpinner = findViewById(R.id.categoryTypeSpinner);
        subCategorySpinner = findViewById(R.id.subCategorySpinner);
        contactTypeSpinner = findViewById(R.id.contactTypeSpinner);
        conditionSpinner = findViewById(R.id.conditionSpinner);
        tradeTypeSpinner = findViewById(R.id.tradeTypeSpinner);
        postButton = findViewById(R.id.postButton);
        addImageButton = findViewById(R.id.addImageButton);
        selectedImagePreview = findViewById(R.id.selectedImagePreview);

        // Setup trade type spinner
        String[] tradeTypes = {"🛒 Buy", "💰 Sell", "🔄 Exchange/Swap", "📋 Rent", "🎁 Free", "🙏 Request"};
        ArrayAdapter<String> tradeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, tradeTypes
        );
        tradeTypeSpinner.setAdapter(tradeAdapter);

        // Hide/show exchange field based on trade type
        tradeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = tradeTypes[position];
                if (selected.contains("Exchange") || selected.contains("Swap")) {
                    exchangeInput.setVisibility(View.VISIBLE);
                    findViewById(R.id.exchangeLabel).setVisibility(View.VISIBLE);
                } else {
                    exchangeInput.setVisibility(View.GONE);
                    findViewById(R.id.exchangeLabel).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup category type spinner
        String[] types = {CategoryManager.TYPE_SERVICE, CategoryManager.TYPE_VEHICLE, 
                          CategoryManager.TYPE_ITEM, CategoryManager.TYPE_CLASSIFIED};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, types
        );
        categoryTypeSpinner.setAdapter(typeAdapter);

        categoryTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = types[position];
                updateSubCategories(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup contact type spinner
        String[] contactTypes = {"Phone", "WhatsApp", "Email", "Telegram", "WeChat", "Signal"};
        ArrayAdapter<String> contactAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, contactTypes
        );
        contactTypeSpinner.setAdapter(contactAdapter);

        // Setup condition spinner
        String[] conditions = {"New", "Like New", "Excellent", "Good", "Fair", "Needs Work"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, conditions
        );
        conditionSpinner.setAdapter(conditionAdapter);

        // Set default location
        locationInput.setText("Colombo, Sri Lanka");

        // Image picker
        addImageButton.setOnClickListener(v -> pickImage());

        // Remove image on long click
        selectedImagePreview.setOnLongClickListener(v -> {
            selectedImagePreview.setImageDrawable(null);
            selectedImagePreview.setVisibility(View.GONE);
            selectedImages.clear();
            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
            return true;
        });

        postButton.setOnClickListener(v -> postAd());
    }

    private void updateSubCategories(String type) {
        String[] subCats = categories.get(type);
        if (subCats != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_dropdown_item, subCats
            );
            subCategorySpinner.setAdapter(adapter);
        }
    }

    private void pickImage() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Bitmap bitmap = ImageUploadHelper.getBitmapFromUri(this, selectedImageUri);
                if (bitmap != null) {
                    selectedImages.clear();
                    selectedImages.add(bitmap);
                    selectedImagePreview.setImageBitmap(bitmap);
                    selectedImagePreview.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "✅ Image added!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void postAd() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String price = priceInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String type = categoryTypeSpinner.getSelectedItem().toString();
        String category = subCategorySpinner.getSelectedItem().toString();
        String contactType = contactTypeSpinner.getSelectedItem().toString();
        String condition = conditionSpinner.getSelectedItem().toString();
        String tradeType = tradeTypeSpinner.getSelectedItem().toString();
        String exchangeFor = exchangeInput.getText().toString().trim();

        // Validate
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contact.isEmpty()) {
            Toast.makeText(this, "Please enter contact info", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((tradeType.contains("Exchange") || tradeType.contains("Swap")) && exchangeFor.isEmpty()) {
            Toast.makeText(this, "Please enter what you want in exchange", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create ad
        String fullCategory = type + " > " + category;
        AdModel ad = new AdModel(
            title, description, fullCategory, price,
            contact, contactType,
            currentLat, currentLng
        );
        ad.setCondition(condition);
        ad.setLocation(location);
        ad.setAdType(type);
        ad.setTradeType(tradeType);
        ad.setExchangeFor(exchangeFor);

        // Add images
        if (!selectedImages.isEmpty()) {
            List<String> encodedImages = ImageUploadHelper.encodeImagesToBase64(selectedImages);
            ad.setImages(encodedImages);
        }

        adManager.addAd(ad);

        Toast.makeText(this, "✅ " + tradeType + " posted with " + selectedImages.size() + " images!", Toast.LENGTH_LONG).show();
        finish();
    }
}
