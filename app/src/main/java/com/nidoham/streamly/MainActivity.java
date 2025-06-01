package com.nidoham.streamly;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.nidoham.streamly.databinding.ActivityMainBinding;
import com.nidoham.streamly.fragments.adapters.BottomNavigationFragmentsAdapter;
import com.nidoham.streamly.fragments.ui.HomeFragment;
import com.nidoham.streamly.fragments.ui.ReelFragment;
import com.nidoham.streamly.fragments.ui.SubscriptionsFragment;
import com.nidoham.streamly.fragments.ui.DownloadFragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private ViewPager2 viewPager;
    private BottomNavigationFragmentsAdapter adapter;

    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewPager2
        viewPager = binding.mainContent;
        // Create fragments in EXACT menu order
        final Fragment[] fragments = {
            new HomeFragment(),          // Position 0 - nav_home
            new ReelFragment(),          // Position 1 - nav_reel
            new SubscriptionsFragment(), // Position 2 - nav_subscriptions
            new DownloadFragment()       // Position 3 - nav_download
        };
        // Setup adapter
        adapter = new BottomNavigationFragmentsAdapter(this, fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getItemCount()); // Keep all fragments in memory
        viewPager.setUserInputEnabled(false); // Disable swiping between fragments

        setupToolbar();
        setupDrawerNavigation();
        setupBottomNavigation();
        setInitialSelection();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.mainToolbar);

        binding.drawerButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.searchButton.setOnClickListener(v -> {
            // TODO: Implement search functionality
        });

        binding.notificationsButton.setOnClickListener(v -> {
            // TODO: Implement notifications functionality
        });
    }

    private void setupDrawerNavigation() {
        drawerLayout = binding.drawerLayout;
        final NavigationView navigationView = binding.navigationView;

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            updateDrawerHeader();
        }
    }

    private void updateDrawerHeader() {
        try {
            final View header = binding.navigationView.getHeaderView(0);
            if (header != null) {
                final CircleImageView profileImage = header.findViewById(R.id.imageView);
                final TextView username = header.findViewById(R.id.textUsername);
                final TextView status = header.findViewById(R.id.indicator);
                if (username != null) {
                    username.setText("John Doe");
                }

                if (status != null) {
                    status.setText("Premium Member");
                }

                header.setOnClickListener(v -> {
                    // TODO: Implement profile functionality
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                });
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void setupBottomNavigation() {
        final BottomNavigationView bottomNav = binding.bottomNav;
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                final int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    viewPager.setCurrentItem(0, true);
                    return true;
                } else if (itemId == R.id.nav_reel) {
                    viewPager.setCurrentItem(1, true);
                    return true;
                } else if (itemId == R.id.nav_subscriptions) {
                    viewPager.setCurrentItem(2, true);
                    return true;
                } else if (itemId == R.id.nav_download) {
                    viewPager.setCurrentItem(3, true);
                    return true;
                }
                return false;
            });
            // Sync ViewPager position changes with BottomNavigation
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(final int position) {
                    super.onPageSelected(position);
                    switch (position) {
                        case 0:
                            bottomNav.setSelectedItemId(R.id.nav_home);
                            break;
                        case 1:
                            bottomNav.setSelectedItemId(R.id.nav_reel);
                            break;
                        case 2:
                            bottomNav.setSelectedItemId(R.id.nav_subscriptions);
                            break;
                        case 3:
                            bottomNav.setSelectedItemId(R.id.nav_download);
                            break;
                    }
                }
            });
        }
    }

    private void setInitialSelection() {
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }
        viewPager.setCurrentItem(0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            viewPager.setCurrentItem(0, true);
        } else if (itemId == R.id.nav_reel) {
            viewPager.setCurrentItem(1, true);
        } else if (itemId == R.id.nav_subscriptions) {
            viewPager.setCurrentItem(2, true);
        } else if (itemId == R.id.nav_download) {
            viewPager.setCurrentItem(3, true);
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
