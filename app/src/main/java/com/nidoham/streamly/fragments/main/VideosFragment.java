package com.nidoham.streamly.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nidoham.streamly.R;
import com.nidoham.streamly.databinding.FragmentVideosBinding;
import com.nidoham.streamly.fragments.videos.FoldersFragment;
import com.nidoham.streamly.fragments.videos.PlaylistsFragment;
import com.nidoham.streamly.fragments.videos.VideoFragment;

public class VideosFragment extends Fragment {
    private FragmentVideosBinding binding;
    private TabLayoutMediator mediator;

    public VideosFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVideosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Use binding to access views
        ViewPager2 viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;
        
        // Set up the adapter
        FragmentVideoPageViewerAdapter adapter = new FragmentVideoPageViewerAdapter(this);
        viewPager.setAdapter(adapter);
        
        // Disable swiping in ViewPager2 (optional, based on your requirements)
        viewPager.setUserInputEnabled(false);
        
        // Reduce ViewPager2 sensitivity to prevent accidental swipes
        viewPager.setOffscreenPageLimit(3);
        
        // Connect the TabLayout with the ViewPager2
        mediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Set tab titles programmatically
            switch (position) {
                case FragmentVideoPageViewerAdapter.VIDEOS_TAB:
                    tab.setText(R.string.tab_videos);
                    break;
                case FragmentVideoPageViewerAdapter.FOLDERS_TAB:
                    tab.setText(R.string.tab_folders);
                    break;
                case FragmentVideoPageViewerAdapter.PLAYLISTS_TAB:
                    tab.setText(R.string.tab_playlists);
                    break;
            }
        });
        mediator.attach();
    }

    @Override
    public void onDestroyView() {
        // Detach mediator to prevent memory leaks
        if (mediator != null) {
            mediator.detach();
        }
        super.onDestroyView();
        binding = null;
    }

    /**
     * FragmentVideoPageViewerAdapter - Adapter for handling tab navigation between
     * Videos, Folders, and Playlists fragments.
     * 
     * Compatible with both Android Phone and TV platforms.
     */
    public static class FragmentVideoPageViewerAdapter extends FragmentStateAdapter {
        
        private static final int TAB_COUNT = 3;
        
        // Tab positions
        public static final int VIDEOS_TAB = 0;
        public static final int FOLDERS_TAB = 1;
        public static final int PLAYLISTS_TAB = 2;
        
        /**
         * Constructor for the adapter
         *
         * @param fragment The fragment that hosts the ViewPager2
         */
        public FragmentVideoPageViewerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }
        
        /**
         * Creates the appropriate fragment based on the tab position
         *
         * @param position The position of the tab
         * @return The fragment corresponding to the selected tab
         */
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case VIDEOS_TAB:
                    return new VideoFragment();
                case FOLDERS_TAB:
                    return new FoldersFragment();
                case PLAYLISTS_TAB:
                    return new PlaylistsFragment();
                default:
                    // Default to videos if something goes wrong
                    return new VideoFragment();
            }
        }
        
        /**
         * Returns the total number of tabs
         *
         * @return The number of tabs to display
         */
        @Override
        public int getItemCount() {
            return TAB_COUNT;
        }
    }
}