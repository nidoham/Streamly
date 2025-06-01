package com.nidoham.streamly.fragments.ui;

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
import androidx.recyclerview.widget.RecyclerView;

import com.nidoham.streamly.R;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView trendingRecyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        trendingRecyclerView = view.findViewById(R.id.trending_recycler_view);
        trendingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTrendingVideos();
    }

    private void loadTrendingVideos() {
        final int youtubeServiceId = 0;
        final String trendingKioskId = "Trending";

        new Thread(() -> {
            final StreamingService service;
            final KioskExtractor extractor;
            final List<StreamInfoItem> items;

            try {
                if (NewPipe.getDownloader() == null) {
                    Log.e(TAG, "NewPipe Downloader is not initialized!");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(
                                        getContext(),
                                        "Extractor not initialized",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                    return;
                }

                service = NewPipe.getService(youtubeServiceId);
                if (service == null || service.getKioskList() == null) {
                    Log.e(TAG, "YouTube service or KioskList not available.");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(
                                        getContext(),
                                        "Service not available",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                    return;
                }

                extractor = service.getKioskList().getExtractorById(trendingKioskId, null);
                if (extractor == null) {
                    Log.e(TAG, "Could not get KioskExtractor for: " + trendingKioskId);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(
                                        getContext(),
                                        "Trending extractor not found",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                    return;
                }

                Log.d(TAG, "Fetching trending videos...");
                extractor.fetchPage();
                items = extractor.getInitialPage().getItems();

                Log.d(TAG, "Fetched " + (items != null ? items.size() : 0) + " trending items.");
                final List<StreamInfoItem> finalItems = items;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (finalItems != null && !finalItems.isEmpty()) {
                            Toast.makeText(
                                    getContext(),
                                    "Loaded " + finalItems.size() + " videos",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "No trending videos found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                }

            } catch (final IOException | ExtractionException e) {
                Log.e(TAG, "Error fetching trending videos", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getContext(),
                                    "Error loading videos: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            } catch (final Exception e) {
                Log.e(TAG, "An unexpected error occurred", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getContext(),
                                    "An unexpected error occurred",
                                    Toast.LENGTH_LONG
                            ).show()
                    );
                }
            }
        }).start();
    }
}
