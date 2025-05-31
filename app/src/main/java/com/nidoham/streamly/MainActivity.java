package com.nidoham.streamly;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.nidoham.streamly.adapters.fragments.MainPageViewer;
import com.nidoham.streamly.databinding.ActivityMainBinding;
import com.nidoham.streamly.databinding.NavHeaderBinding;

public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;     
    private NavHeaderBinding drawerBinding;
    private MainPageViewer pageAdapter;
    private boolean isTvMode;
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Check if running on TV
        isTvMode = isRunningOnTv();
        
        initializeComponents();
        setupUI();
    }
    
    private boolean isRunningOnTv() {
        int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK;
        return uiMode == Configuration.UI_MODE_TYPE_TELEVISION;
    }
    
    private void initializeComponents() {
        // Initialize MainPageViewer adapter
        pageAdapter = new MainPageViewer(this);
    }
    
    private void setupUI() {
        setSupportActionBar(binding.toolbar);
        setupNavigationDrawer();
        setupViewPager();
        
        // Apply TV-specific customizations
        if (isTvMode) {
            setupForTv();
        }
    }
    
    private void setupForTv() {
        // Increase touch target sizes for TV remote navigation
        int largePadding = 16; // Use fixed value instead of dimension resource
        
        // Apply larger paddings to all clickable elements
        binding.drawerIcon.setPadding(largePadding, largePadding, largePadding, largePadding);
        binding.searchIcon.setPadding(largePadding, largePadding, largePadding, largePadding);
        binding.filterIcon.setPadding(largePadding, largePadding, largePadding, largePadding);
        binding.btnMoreOptions.setPadding(largePadding, largePadding, largePadding, largePadding);
        
        // Hide bottom navigation if it exists (TV typically uses D-pad navigation)
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setVisibility(View.GONE);
        }
        
        // Set initial focus for TV remote
        binding.drawerIcon.requestFocus();
    }
    
    private void setupViewPager() {
        // Set adapter to ViewPager2
        binding.viewPager.setAdapter(pageAdapter);
        
        // Disable swiping for all devices
        binding.viewPager.setUserInputEnabled(false);
        
        // Set default page
        binding.viewPager.setCurrentItem(MainPageViewer.VIDEOS_PAGE, false);
        updateToolbarTitle(MainPageViewer.VIDEOS_PAGE);
        
        // Handle page changes
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateToolbarTitle(position);
                
                // Update bottom navigation if it exists
                if (binding.bottomNavigation != null) {
                    switch (position) {
                        case MainPageViewer.VIDEOS_PAGE:
                            binding.bottomNavigation.setSelectedItemId(R.id.nav_videos);
                            break;
                        case MainPageViewer.MUSIC_PAGE:
                            binding.bottomNavigation.setSelectedItemId(R.id.nav_music);
                            break;
                        case MainPageViewer.ME_PAGE:
                            binding.bottomNavigation.setSelectedItemId(R.id.nav_me);
                            break;
                    }
                }
            }
        });
        
        // Set up bottom navigation if it exists
        if (binding.bottomNavigation != null) {
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_videos) {
                    binding.viewPager.setCurrentItem(MainPageViewer.VIDEOS_PAGE, false);
                    return true;
                } else if (itemId == R.id.nav_music) {
                    binding.viewPager.setCurrentItem(MainPageViewer.MUSIC_PAGE, false);
                    return true;
                } else if (itemId == R.id.nav_me) {
                    binding.viewPager.setCurrentItem(MainPageViewer.ME_PAGE, false);
                    return true;
                }
                return false;
            });
        }
    }
    
    private void updateToolbarTitle(int position) {
        // Update toolbar title based on selected tab
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(pageAdapter.getPageTitle(position));
        }
    }
    
    private void refreshContent(int position) {
        // Refresh content based on the tab that was reselected
        // This can be implemented to scroll to top or reload data
        Toast.makeText(this, "Refreshing " + pageAdapter.getPageTitle(position), 
                Toast.LENGTH_SHORT).show();
    }
    
    private void setupNavigationDrawer() {
        drawerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0));
        binding.drawerIcon.setOnClickListener(v -> toggleDrawer());
        binding.navView.setNavigationItemSelectedListener(item -> {
            handleNavigationItem(item.getItemId());
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        
        // Set focus behavior for TV navigation
        if (isTvMode) {
            binding.navView.setFocusable(true);
            binding.navView.setFocusableInTouchMode(true);
        }
        
        // Set up more options button
        binding.btnMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v, GravityCompat.END);
            popup.getMenuInflater().inflate(R.menu.main_options_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this::handleMenuItemClick);
            popup.show();
        });
        
        // Set up search icon
        binding.searchIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Search Selected", Toast.LENGTH_SHORT).show();
            // Launch search activity or show search dialog
        });
        
        // Set up filter icon
        binding.filterIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Filter Selected", Toast.LENGTH_SHORT).show();
            // Show filter options
        });
    }
    
    private void toggleDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }
    
    private void handleNavigationItem(int itemId) {
        // Handle specific navigation items
        
        if (itemId == R.id.nav_favorites) {
            Toast.makeText(this, "Favorites Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_history) {
            Toast.makeText(this, "History Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_settings) {
            Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_about) {
            Toast.makeText(this, "About Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_subscription) {
            Toast.makeText(this, "Subscription Selected", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, "Logout Selected", Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean handleMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_settings) {
            Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_help) {
            Toast.makeText(this, "Help Selected", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_share) {
            shareApp();
            return true;
        } else if (itemId == R.id.action_feedback) {
            sendFeedback();
            return true;
        }
        
        return false;
    }
    
    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
                "Check out " + getString(R.string.app_name) + 
                " app: https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
    
    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback@streamly.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Device: " + android.os.Build.MODEL + "\n" +
                "Android Version: " + android.os.Build.VERSION.RELEASE + "\n\n" +
                "Feedback: ");
        try {
            startActivity(Intent.createChooser(intent, "Send Feedback"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Close drawer if open, otherwise perform default back action
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    // Handle D-pad navigation for TV devices
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isTvMode) {
            int keyCode = event.getKeyCode();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                // Handle DPAD navigation
                // Removed the page change functionality for DPAD left/right
                
                // Menu button opens drawer
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    toggleDrawer();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
}