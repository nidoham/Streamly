package com.nidoham.streamly.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.nidoham.streamly.R;
import com.nidoham.streamly.VideoPlayerActivity;
import com.nidoham.streamly.adapter.VideosAdapter;
import com.nidoham.streamly.videos.Videos;
import com.nidoham.streamly.model.VideoModel;
import com.nidoham.streamly.databinding.FragmentsVideoBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoFragment extends Fragment {

    private static final String TAG = "VideoFragment";
    private static final String PREFS_NAME = "StreamlyAppPrefs";
    private static final String KEY_LAST_PLAYED_VIDEO_ID = "lastPlayedVideoId";
    private static final String KEY_LAST_PLAYED_POSITION = "lastPlayedPosition";
    private static final String KEY_CONTINUE_WATCHING_ENABLED = "continueWatchingEnabled";
    
    private VideosAdapter adapter;
    private Videos videosUtility;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences sharedPreferences;
    
    // Device type detection
    private boolean isTV = false;
    private boolean isLandscape = false;
    
    // Video list
    private List<VideoModel> videoList = new ArrayList<>();
    private VideoModel lastPlayedVideo = null;

    private FragmentsVideoBinding binding;

    public VideoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentsVideoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Check if device is a TV
        isTV = requireContext().getPackageManager().hasSystemFeature("android.software.leanback");
        isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        videosUtility = new Videos(requireContext());
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Set up FAB click listener
        binding.fabMxPlayer.setOnClickListener(v -> handleFabClick());
        
        // Apply TV-specific UI adjustments
        if (isTV) {
            applyTVSpecificSettings();
        }
        
        // Initialize app features directly without permission checks
        initializeAppFeatures();
    }
    
    private void handleFabClick() {
        if (videoList.isEmpty()) {
            Toast.makeText(requireContext(), "No videos available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if we have a last played video
        String lastPlayedId = sharedPreferences.getString(KEY_LAST_PLAYED_VIDEO_ID, null);
        if (lastPlayedId != null) {
            // Find the last played video in the list
            for (VideoModel video : videoList) {
                if (video.getId().equals(lastPlayedId)) {
                    lastPlayedVideo = video;
                    break;
                }
            }
        }
        
        if (lastPlayedVideo != null) {
            // Play the last watched video
            playVideo(lastPlayedVideo);
            Toast.makeText(requireContext(), "Resuming: " + lastPlayedVideo.getTitle(), Toast.LENGTH_SHORT).show();
        } else if (!videoList.isEmpty()) {
            // If no last played video, play the first video in the list
            playVideo(videoList.get(0));
        }
    }
    
    private void applyTVSpecificSettings() {
        // Make FAB more accessible for TV navigation
        binding.fabMxPlayer.setFocusable(true);
        binding.fabMxPlayer.setFocusableInTouchMode(true);
    }

    private void initializeAppFeatures() {
        binding.recyclerViewVideos.setVisibility(View.VISIBLE);
        binding.fabMxPlayer.setVisibility(View.VISIBLE);

        setupRecyclerView();
        loadVideos();
    }

    private void setupRecyclerView() {
        // Determine grid columns based on device type and orientation
        int gridColumns = calculateGridColumns();
        
        adapter = new VideosAdapter(new VideosAdapter.OnVideoClickListener() {
            @Override
            public void onVideoClick(VideoModel video) {
                playVideo(video);
            }

            @Override
            public void onVideoLongClick(VideoModel video) {
                showVideoOptionsMenu(video);
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), gridColumns);
        binding.recyclerViewVideos.setLayoutManager(layoutManager);
        binding.recyclerViewVideos.setAdapter(adapter);
        
        // For TV, add extra padding and focus handling
        if (isTV) {
            binding.recyclerViewVideos.setPadding(16, 16, 16, 16);
            
            // Set initial focus to the first item when videos are loaded
            adapter.registerAdapterDataObserver(new androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (adapter.getItemCount() > 0) {
                        binding.recyclerViewVideos.requestFocus();
                    }
                }
            });
        }
    }
    
    private int calculateGridColumns() {
        // Determine optimal grid columns based on device type and orientation
        if (isTV) {
            return 4; // More columns for TV
        } else {
            // For phones/tablets
            return isLandscape ? 3 : 2;
        }
    }
    
    private void showVideoOptionsMenu(VideoModel video) {
        // Create a popup menu with options
        android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), binding.fabMxPlayer);
        popup.getMenu().add("Play Video");
        popup.getMenu().add("Play Next");
        popup.getMenu().add("Add to Playlist");
        popup.getMenu().add("Share");
        
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Play Video")) {
                playVideo(video);
            } else if (title.equals("Play Next")) {
                // Save as next to play
                Toast.makeText(requireContext(), "Added to play next", Toast.LENGTH_SHORT).show();
            } else if (title.equals("Add to Playlist")) {
                // Show playlist selection dialog
                Toast.makeText(requireContext(), "Playlist feature coming soon", Toast.LENGTH_SHORT).show();
            } else if (title.equals("Share")) {
                shareVideo(video);
            }
            return true;
        });
        
        popup.show();
    }
    
    private void shareVideo(VideoModel video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this video");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + video.getTitle());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
    
    private void playVideo(VideoModel video) {
        // Save the last played video ID
        sharedPreferences.edit()
            .putString(KEY_LAST_PLAYED_VIDEO_ID, video.getId())
            .apply();
        
        // Update the lastPlayedVideo reference
        lastPlayedVideo = video;
        
        // Create intent with video details
        Intent intent = new Intent(requireContext(), VideoPlayerActivity.class);
        intent.putExtra("videoTitle", video.getTitle());
        intent.putExtra("videoPath", video.getVideoPath());
        
        // Add playlist support - pass all videos as a playlist
        ArrayList<VideoModel> playlist = new ArrayList<>(videoList);
        intent.putParcelableArrayListExtra("playlist", playlist);
        
        // Find the index of the selected video in the playlist
        int currentIndex = 0;
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).getId().equals(video.getId())) {
                currentIndex = i;
                break;
            }
        }
        intent.putExtra("currentIndex", currentIndex);
        
        // Get last position if available
        long lastPosition = sharedPreferences.getLong(KEY_LAST_PLAYED_POSITION + "_" + video.getId(), 0);
        if (lastPosition > 0) {
            intent.putExtra("startPosition", lastPosition);
        }
        
        startActivity(intent);
    }

    private void loadVideos() {
        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.fabMxPlayer.setVisibility(View.GONE); // Hide FAB while loading
        
        executorService.execute(() -> {
            try {
                videoList = videosUtility.loadVideos();
                mainThreadHandler.post(() -> {
                    // Hide loading indicator
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (adapter != null) {
                        adapter.submitList(videoList);
                    }
                    
                    if (videoList.isEmpty()) {
                        Toast.makeText(requireContext(), "No videos found.", Toast.LENGTH_SHORT).show();
                        binding.tvNoVideos.setVisibility(View.VISIBLE);
                        binding.fabMxPlayer.setVisibility(View.GONE); // Keep FAB hidden if no videos
                    } else {
                        binding.tvNoVideos.setVisibility(View.GONE);
                        binding.fabMxPlayer.setVisibility(View.VISIBLE); // Show FAB when videos are available
                        
                        // For TV, set focus on the first item
                        if (isTV && binding.recyclerViewVideos.getChildCount() > 0) {
                            binding.recyclerViewVideos.getChildAt(0).requestFocus();
                        }
                        
                        // Check for last played video
                        findLastPlayedVideo();
                        
                        // Check if we should auto-resume last played video (for TV experience)
                        if (isTV && sharedPreferences.getBoolean(KEY_CONTINUE_WATCHING_ENABLED, true)) {
                            checkAutoResumeLastVideo();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading videos", e);
                mainThreadHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.fabMxPlayer.setVisibility(View.GONE); // Hide FAB on error
                    Toast.makeText(requireContext(), "Error loading videos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void findLastPlayedVideo() {
        String lastPlayedId = sharedPreferences.getString(KEY_LAST_PLAYED_VIDEO_ID, null);
        if (lastPlayedId != null && !videoList.isEmpty()) {
            // Find the last played video in the list
            for (VideoModel video : videoList) {
                if (video.getId().equals(lastPlayedId)) {
                    lastPlayedVideo = video;
                    
                    // Update FAB appearance to indicate resume functionality
                    binding.fabMxPlayer.setImageResource(R.drawable.ic_play);
                    binding.fabMxPlayer.setContentDescription("Resume last video");
                    
                    break;
                }
            }
        }
    }
    
    private void checkAutoResumeLastVideo() {
        // For TV experience, optionally auto-resume last played video
        if (lastPlayedVideo != null && isAdded()) {
            // Show a dialog asking if user wants to resume
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Continue Watching");
            builder.setMessage("Would you like to resume watching " + lastPlayedVideo.getTitle() + "?");
            builder.setPositiveButton("Resume", (dialog, which) -> {
                playVideo(lastPlayedVideo);
            });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            
            // Add "Don't ask again" checkbox
            builder.setNeutralButton("Don't ask again", (dialog, which) -> {
                sharedPreferences.edit().putBoolean(KEY_CONTINUE_WATCHING_ENABLED, false).apply();
            });
            
            builder.show();
        }
    }
    
    // Handle D-pad navigation for TV
    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        // Improve D-pad navigation for TV
        if (isTV) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && binding.recyclerViewVideos.getChildCount() > 0) {
                // If at the top row and pressing up, handle specially
                View firstVisibleItem = binding.recyclerViewVideos.getChildAt(0);
                if (firstVisibleItem.hasFocus() && 
                    binding.recyclerViewVideos.getChildAdapterPosition(firstVisibleItem) < calculateGridColumns()) {
                    // We're in the first row, focus on something else if needed
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                // If at the bottom row, allow focus to move to FAB
                View lastVisibleItem = binding.recyclerViewVideos.getChildAt(binding.recyclerViewVideos.getChildCount() - 1);
                if (lastVisibleItem != null && lastVisibleItem.hasFocus()) {
                    int position = binding.recyclerViewVideos.getChildAdapterPosition(lastVisibleItem);
                    int totalItems = adapter.getItemCount();
                    int columns = calculateGridColumns();
                    
                    // Check if we're in the last row
                    if (position >= totalItems - columns) {
                        binding.fabMxPlayer.requestFocus();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Update layout when orientation changes
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        
        // If recycler view is already set up, update the grid columns
        if (binding != null && binding.recyclerViewVideos.getLayoutManager() != null) {
            int newColumns = calculateGridColumns();
            ((GridLayoutManager) binding.recyclerViewVideos.getLayoutManager()).setSpanCount(newColumns);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Check if we need to refresh the video list
        if (adapter == null || adapter.getItemCount() == 0) {
            loadVideos();
        } else {
            // Just update the last played video reference
            findLastPlayedVideo();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}