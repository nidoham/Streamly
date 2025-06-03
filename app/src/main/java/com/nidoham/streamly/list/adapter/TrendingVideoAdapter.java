package com.nidoham.streamly.list.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Import Picasso for image loading
// Add dependency: implementation 'com.squareup.picasso:picasso:2.8' (or latest)
import com.squareup.picasso.Picasso;

import com.nidoham.streamly.R; // Make sure R is imported correctly

import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrendingVideoAdapter extends RecyclerView.Adapter<TrendingVideoAdapter.VideoViewHolder> {

    private List<StreamInfoItem> videoItems = new ArrayList<>();
    private final Context context;
    // Optional: Add a listener for item clicks
    // private OnItemClickListener listener;

    // public interface OnItemClickListener {
    //     void onItemClick(StreamInfoItem item);
    // }

    public TrendingVideoAdapter(Context context /*, OnItemClickListener listener */) {
        this.context = context;
        // this.listener = listener;
    }

    public void setItems(List<StreamInfoItem> items) {
        this.videoItems.clear();
        if (items != null) {
            this.videoItems.addAll(items);
        }
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your custom list item layout here
        // You need to create 'list_item_video.xml' in your res/layout folder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        StreamInfoItem item = videoItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    // ViewHolder class
    class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView uploaderTextView;
        TextView detailsTextView; // For views, date etc.
        TextView durationTextView;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID from your list_item_video.xml
            thumbnailImageView = itemView.findViewById(R.id.videoThumbnail);
            titleTextView = itemView.findViewById(R.id.videoTitle);
            uploaderTextView = itemView.findViewById(R.id.videoUploader);
            detailsTextView = itemView.findViewById(R.id.videoDetails);
            durationTextView = itemView.findViewById(R.id.videoDuration);
        }

        void bind(final StreamInfoItem item) {
            titleTextView.setText(item.getName());
            uploaderTextView.setText(item.getUploaderName());

            // Construct details string (Example: Views • Upload Date)
            String details = "";
            if (item.getViewCount() >= 0) {
                details += formatViewCount(item.getViewCount()) + " views";
            }
            if (item.getUploadDate() != null) {
                if (!details.isEmpty()) {
                    details += " • ";
                }
                // Use date() method instead of getText()
                details += item.getUploadDate().date().toString(); // Or format it as needed
            }
            detailsTextView.setText(details);
            
            // Set duration overlay
            if (item.getDuration() > 0) {
                durationTextView.setText(formatDuration(item.getDuration()));
                durationTextView.setVisibility(View.VISIBLE);
            } else {
                durationTextView.setVisibility(View.GONE);
            }

            // Load thumbnail using Picasso
            String thumbnailUrl = "";
            if (item.getThumbnails() != null && !item.getThumbnails().isEmpty()) {
                thumbnailUrl = item.getThumbnails().get(0).getUrl();
            }
            
            Picasso.get()
                    .load(thumbnailUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Use built-in placeholder
                    .error(android.R.drawable.ic_menu_close_clear_cancel) // Use built-in error drawable
                    .fit()
                    .centerCrop()
                    .into(thumbnailImageView);

            // Optional: Set click listener
            // itemView.setOnClickListener(v -> {
            //     if (listener != null) {
            //         listener.onItemClick(item);
            //     }
            // });
        }
        
        // Helper to format view count (optional)
        private String formatViewCount(long count) {
            if (count < 1000) return String.valueOf(count);
            int exp = (int) (Math.log(count) / Math.log(1000));
            return String.format(Locale.US, "%.1f%c", count / Math.pow(1000, exp), "KMBTPE".charAt(exp-1));
        }

        // Helper to format duration (seconds to HH:MM:SS or MM:SS)
        private String formatDuration(long seconds) {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            if (hours > 0) {
                return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs);
            } else {
                return String.format(Locale.US, "%02d:%02d", minutes, secs);
            }
        }
    }
}