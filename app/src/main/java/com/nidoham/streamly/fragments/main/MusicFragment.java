package com.nidoham.streamly.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nidoham.streamly.R;
import com.nidoham.streamly.databinding.FragmentMusicBinding;

public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;

    public MusicFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMusicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI components
        setupUI();
        
        // Load music data
        loadMusic();
    }

    private void setupUI() {
        // Setup RecyclerView, player controls, etc.
    }

    private void loadMusic() {
        // Load music from repository or API
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}