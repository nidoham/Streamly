package com.nidoham.streamly;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.nidoham.streamly.databinding.ActivityMainBinding;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                    handleHomeSelection();
                    return true;
                } else if (itemId == R.id.nav_reel) {
                    handleReelSelection();
                    return true;
                } else if (itemId == R.id.nav_subscriptions) {
                    handleSubscriptionsSelection();
                    return true;
                } else if (itemId == R.id.nav_download) {
                    handleDownloadsSelection();
                    return true;
                }
                return false;
            });
        }
    }

    private void setInitialSelection() {
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }
        handleHomeSelection();
    }

    private void handleHomeSelection() {
        // TODO: Implement home functionality
    }

    private void handleReelSelection() {
        // TODO: Implement reel functionality
    }

    private void handleSubscriptionsSelection() {
        // TODO: Implement subscriptions functionality
    }

    private void handleDownloadsSelection() {
        // TODO: Implement downloads functionality
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            handleHomeSelection();
            updateBottomNavSelection(R.id.nav_home);
        } else if (itemId == R.id.nav_reel) {
            handleReelSelection();
            updateBottomNavSelection(R.id.nav_reel);
        } else if (itemId == R.id.nav_subscriptions) {
            handleSubscriptionsSelection();
            updateBottomNavSelection(R.id.nav_subscriptions);
        } else if (itemId == R.id.nav_download) {
            handleDownloadsSelection();
            updateBottomNavSelection(R.id.nav_download);
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void updateBottomNavSelection(final int itemId) {
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(itemId);
        }
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
