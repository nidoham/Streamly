package com.nidoham.streamly.fragments.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class BottomNavigationFragmentsAdapter extends FragmentStateAdapter {
    private final Fragment[] fragments;

    public BottomNavigationFragmentsAdapter(
            final @NonNull FragmentActivity fragmentActivity,
            final Fragment... fragments
    ) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(final int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }
}
