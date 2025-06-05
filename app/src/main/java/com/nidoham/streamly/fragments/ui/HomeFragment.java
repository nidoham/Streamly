package com.nidoham.streamly.fragments.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nidoham.streamly.R;
import com.nidoham.streamly.databinding.FragmentHomeBinding;
import com.nidoham.streamly.list.adapter.TrendingVideoAdapter;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private static final String TAG = "HomeFragment";
    private static final String PREFS_NAME = "StreamlyPrefs";
    private static final String IS_NEW_USER_KEY = "isNewUser";
    
    private TrendingVideoAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
    
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        
        adapter = new TrendingVideoAdapter(getContext());
        binding.trendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.trendingRecyclerView.setAdapter(adapter);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadVideos();
    }

    private boolean isNewUser() {
        if (getContext() == null) return true;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNew = prefs.getBoolean(IS_NEW_USER_KEY, true);
        if (isNew) {
            prefs.edit().putBoolean(IS_NEW_USER_KEY, false).apply();
        }
        return isNew; 
    }

    private void loadVideos() {
        final int youtubeServiceId = 0;
        final String trendingKioskId = "Trending";

        new Thread(() -> {
            try {
                // Check if NewPipe Downloader is initialized
                if (NewPipe.getDownloader() == null) {
                    Log.e(TAG, "NewPipe Downloader is not initialized!");
                    showToastOnUiThread("NewPipe Downloader is not initialized!");
                    return;
                }

                // Get YouTube service
                StreamingService service = NewPipe.getService(youtubeServiceId);
                if (service == null || service.getKioskList() == null) {
                    Log.e(TAG, "YouTube service or KioskList not available.");
                    showToastOnUiThread("Service not available");
                    return;
                }

                List<StreamInfoItem> items = new ArrayList<>();
                boolean isNew = isNewUser();
                String logTagSuffix = isNew ? " (New User - Country Trending)" : " (Old User - Mixed Categories)";

                if (isNew) {
                    // New User: Fetch country-specific trending videos
                    ContentCountry preferredCountry = NewPipe.getPreferredContentCountry();
                    Log.d(TAG, "Fetching trending videos for country: " + 
                          (preferredCountry != null ? preferredCountry.getCountryCode() : "Default") + logTagSuffix);
                    
                    KioskExtractor extractor = service.getKioskList().getExtractorById(trendingKioskId, null);
                    if (extractor != null) {
                        extractor.fetchPage();
                        List<StreamInfoItem> trendingItems = extractor.getInitialPage().getItems();
                        if (trendingItems != null) {
                            items.addAll(trendingItems);
                            Log.d(TAG, "Fetched " + trendingItems.size() + " items from " + trendingKioskId + logTagSuffix);
                        }
                    } else {
                        Log.e(TAG, "Could not get KioskExtractor for: " + trendingKioskId + logTagSuffix);
                    }
                } else {
                    // Old User: Fetch from multiple kiosks for a mix of content
                    List<String> availableKiosks = new ArrayList<>(service.getKioskList().getAvailableKiosks());
                    int numKiosksToFetch = Math.min(3, availableKiosks.size()); // Limit to 3 kiosks
                    Log.d(TAG, "Fetching videos from " + numKiosksToFetch + " kiosks for old user" + logTagSuffix);

                    for (int i = 0; i < numKiosksToFetch; i++) {
                        String kioskId = availableKiosks.get(i);
                        KioskExtractor extractor = service.getKioskList().getExtractorById(kioskId, null);
                        if (extractor != null) {
                            try {
                                extractor.fetchPage();
                                List<StreamInfoItem> kioskItems = extractor.getInitialPage().getItems();
                                if (kioskItems != null) {
                                    items.addAll(kioskItems);
                                    Log.d(TAG, "Fetched " + kioskItems.size() + " items from " + kioskId + logTagSuffix);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error fetching from kiosk: " + kioskId + logTagSuffix, e);
                            }
                        }
                    }
                }

                // Update UI on the main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) {
                            adapter.setItems(items);
                            Log.d(TAG, "Updated adapter with " + items.size() + " items" + logTagSuffix);
                        } else {
                            Log.w(TAG, "Adapter is null when trying to set items" + logTagSuffix);
                        }
                    });
                }

            } catch (IOException | ExtractionException e) {
                Log.e(TAG, "Error loading videos", e);
                showToastOnUiThread("Error loading videos: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "An unexpected error occurred", e);
                showToastOnUiThread("An unexpected error occurred");
            }
        }).start();
    }

    private void showToastOnUiThread(final String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show()
            );
        }
    }
}