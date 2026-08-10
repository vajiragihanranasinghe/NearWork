package com.example.nearwork;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    ListView favoritesList;
    TextView emptyText;
    Button clearBtn;
    ArrayList<String> favorites = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesList = findViewById(R.id.favoritesList);
        emptyText = findViewById(R.id.emptyText);
        clearBtn = findViewById(R.id.clearBtn);

        loadFavorites();

        favoritesList.setOnItemClickListener((parent, view, position, id) -> {
            String item = favorites.get(position);
            Toast.makeText(this, "📄 " + item, Toast.LENGTH_SHORT).show();
        });

        clearBtn.setOnClickListener(v -> {
            FavoritesManager.getInstance(this).clearFavorites();
            loadFavorites();
            Toast.makeText(this, "🗑️ All favorites cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFavorites() {
        favorites.clear();
        Set<String> favSet = FavoritesManager.getInstance(this).getFavorites();
        
        if (favSet.isEmpty()) {
            favorites.add("No favorites yet");
            favorites.add("⭐ Save jobs by searching");
            emptyText.setVisibility(View.VISIBLE);
            clearBtn.setVisibility(View.GONE);
        } else {
            favorites.addAll(favSet);
            emptyText.setVisibility(View.GONE);
            clearBtn.setVisibility(View.VISIBLE);
        }
        
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                favorites
        );
        favoritesList.setAdapter(adapter);
    }
}
