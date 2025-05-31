package com.nidoham.streamly.adapters.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nidoham.streamly.fragments.main.*;

/**
 * MainPageViewer - ViewPager2 adapter for main app navigation
 * Provides swipeable access to Videos, Music and Me fragments
 */
public class MainPageViewer extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;
    public static final int VIDEOS_PAGE = 0;
    public static final int MUSIC_PAGE = 1;
    public static final int ME_PAGE = 2;

    public MainPageViewer(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment based on position
        switch (position) {
            case VIDEOS_PAGE:
                return new VideosFragment();
            case MUSIC_PAGE:
                return new MusicFragment();
            case ME_PAGE:
                return new MeFragment();
            default:
                return new VideosFragment(); // Default to videos if position is invalid
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    /**
     * Returns the title for the given page position
     * (Useful for TabLayout integration)
     */
    public String getPageTitle(int position) {
        switch (position) {
            case VIDEOS_PAGE:
                return "Videos";
            case MUSIC_PAGE:
                return "Music";
            case ME_PAGE:
                return "Me";
            default:
                return "Unknown";
        }
    }
}