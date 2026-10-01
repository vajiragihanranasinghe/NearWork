package lk.vajira.nearwork;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    BottomNavigationView bottomNav;
    FloatingActionButton fabMain;

    private static final String[] TAB_TITLES = {"Ads", "Places"};

    private PlacesSearchFragment placesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNavigation);
        fabMain = findViewById(R.id.fabMain);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull @Override
            public Fragment createFragment(int position) {
                if (position == 0) return new AdsFeedFragment();
                placesFragment = new PlacesSearchFragment();
                return placesFragment;
            }
            @Override public int getItemCount() { return 2; }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) ->
                tab.setText(TAB_TITLES[pos])).attach();

        viewPager.setCurrentItem(0, false);

        // FAB swaps icon + action per tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                updateFabForTab(position);
            }
        });
        updateFabForTab(0);

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.nav_search) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void updateFabForTab(int position) {
        if (position == 0) {
            // Ads tab → Post Ad
            fabMain.setImageResource(R.drawable.ic_add);
            fabMain.setContentDescription(getString(R.string.btn_post_ad));
            fabMain.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PostAdActivity.class)));
        } else {
            // Places tab → Open Map
            fabMain.setImageResource(R.drawable.ic_map);
            fabMain.setContentDescription("Open map");
            fabMain.setOnClickListener(v -> {
                if (placesFragment != null) {
                    placesFragment.openMap();
                } else {
                    startActivity(new Intent(MainActivity.this, MapActivity.class));
                }
            });
        }
    }
}
