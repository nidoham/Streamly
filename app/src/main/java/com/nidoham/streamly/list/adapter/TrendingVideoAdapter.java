package com.nidoham.streamly.list.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Picasso লাইব্রেরি ইম্পোর্ট করা হচ্ছে ছবি লোড করার জন্য
import com.squareup.picasso.Picasso;

import com.nidoham.streamly.R; // R ফাইল ইম্পোর্ট করা হচ্ছে

// NewPipe এর StreamInfoItem ইম্পোর্ট করা হচ্ছে
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// অ্যাডাপ্টার ক্লাসটি এখন NewPipe এর StreamInfoItem ব্যবহার করার জন্য আপডেট করা হয়েছে
public class TrendingVideoAdapter extends RecyclerView.Adapter<TrendingVideoAdapter.VideoViewHolder> {

    // ভিডিও আইটেমের তালিকা এখন StreamInfoItem টাইপের
    private List<StreamInfoItem> videoItems = new ArrayList<>();
    private final Context context;
    // আইটেম ক্লিকের জন্য লিসেনার (ঐচ্ছিক)
    // private OnItemClickListener listener;

    // public interface OnItemClickListener {
    //     void onItemClick(StreamInfoItem item);
    // }

    // কনস্ট্রাক্টর
    public TrendingVideoAdapter(Context context /*, OnItemClickListener listener */) {
        this.context = context;
        // this.listener = listener;
    }

    /**
     * অ্যাডাপ্টারের ডেটা সেট করার মেথড।
     * @param items StreamInfoItem অবজেক্টের তালিকা।
     */
    public void setItems(List<StreamInfoItem> items) {
        this.videoItems.clear(); // আগের ডেটা মুছে ফেলা হচ্ছে
        if (items != null) {
            this.videoItems.addAll(items); // নতুন ডেটা যোগ করা হচ্ছে
        }
        notifyDataSetChanged(); // ডেটা পরিবর্তনের নোটিফিকেশন দেওয়া হচ্ছে
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // list_item_video.xml লেআউট ইনফ্লেট করে ViewHolder তৈরি করা হচ্ছে
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        // নির্দিষ্ট পজিশনের জন্য StreamInfoItem পাওয়া যাচ্ছে
        StreamInfoItem item = videoItems.get(position);
        // ViewHolder এর bind মেথড কল করে ডেটা সেট করা হচ্ছে
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        // তালিকার মোট আইটেম সংখ্যা রিটার্ন করা হচ্ছে
        return videoItems.size();
    }

    // ViewHolder ক্লাস - প্রতিটি ভিডিও আইটেমের ভিউ ধারণ করে
    class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView uploaderTextView;
        TextView detailsTextView; // ভিউ এবং তারিখ দেখানোর জন্য
        TextView durationTextView;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            // list_item_video.xml থেকে ভিউগুলো খুঁজে বের করা হচ্ছে
            thumbnailImageView = itemView.findViewById(R.id.videoThumbnail);
            titleTextView = itemView.findViewById(R.id.videoTitle);
            uploaderTextView = itemView.findViewById(R.id.videoUploader);
            detailsTextView = itemView.findViewById(R.id.videoDetails);
            durationTextView = itemView.findViewById(R.id.videoDuration);
        }

        /**
         * ViewHolder এ StreamInfoItem থেকে ডেটা বাইন্ড করার মেথড।
         * @param item যে StreamInfoItem অবজেক্টের ডেটা দেখাতে হবে।
         */
        void bind(final StreamInfoItem item) {
            // শিরোনাম এবং আপলোডারের নাম সেট করা হচ্ছে
            titleTextView.setText(item.getName());
            uploaderTextView.setText(item.getUploaderName());

            // বিস্তারিত তথ্য (ভিউ সংখ্যা • আপলোড তারিখ) তৈরি করা হচ্ছে
            String details = "";
            // ভিউ সংখ্যা ফরম্যাট করে যোগ করা হচ্ছে (যদি থাকে)
            if (item.getViewCount() >= 0) {
                details += formatViewCount(item.getViewCount()) + " views";
            }
            
            // আপলোড তারিখ যোগ করা হচ্ছে (যদি থাকে)
            // StreamInfoItem এর getUploadDate() একটি DateWrapper অবজেক্ট রিটার্ন করে
            // কিন্তু DateWrapper ক্লাস পাওয়া যাচ্ছে না, তাই আমরা getTextualUploadDate() ব্যবহার করব
            try {
                String uploadDate = item.getTextualUploadDate();
                if (!TextUtils.isEmpty(uploadDate)) {
                    if (!details.isEmpty()) {
                        details += " • ";
                    }
                    details += uploadDate;
                }
            } catch (Exception e) {
                // যদি getTextualUploadDate() না থাকে, তাহলে আমরা এটা স্কিপ করব
                e.printStackTrace();
            }
            
            detailsTextView.setText(details);

            // ভিডিওর সময়কাল সেট করা হচ্ছে (যদি থাকে)
            if (item.getDuration() > 0) {
                durationTextView.setText(formatDuration(item.getDuration()));
                durationTextView.setVisibility(View.VISIBLE);
            } else {
                durationTextView.setVisibility(View.GONE);
            }

            // Picasso ব্যবহার করে থাম্বনেইল লোড করা হচ্ছে
            // item.getThumbnails() মেথড ব্যবহার করা হচ্ছে
            String thumbnailUrl = null;
            try {
                // getThumbnails() থেকে সবচেয়ে ভালো quality এর thumbnail নিই
                if (item.getThumbnails() != null && !item.getThumbnails().isEmpty()) {
                    // সবচেয়ে বড় resolution এর thumbnail খুঁজি
                    thumbnailUrl = item.getThumbnails().get(item.getThumbnails().size() - 1).getUrl();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                Picasso.get()
                        .load(thumbnailUrl) // StreamInfoItem থেকে থাম্বনেইল URL
                        .placeholder(R.drawable.placeholder_thumbnail_video) // কাস্টম প্লেসহোল্ডার
                        .error(R.drawable.placeholder_thumbnail_video) // কাস্টম এরর ড্রয়েবল
                        .fit()
                        .centerCrop()
                        .into(thumbnailImageView);
            } else {
                 // থাম্বনেইল না থাকলে প্লেসহোল্ডার দেখানো হচ্ছে
                 thumbnailImageView.setImageResource(R.drawable.placeholder_thumbnail_video);
            }

            // আইটেম ক্লিকের জন্য লিসেনার সেট করা (ঐচ্ছিক)
            // itemView.setOnClickListener(v -> {
            //     if (listener != null) {
            //         listener.onItemClick(item);
            //     }
            // });
        }

        // ভিউ সংখ্যা ফরম্যাট করার সাহায্যকারী মেথড (যেমন 1.2K, 3.4M)
        private String formatViewCount(long count) {
            if (count < 1000) return String.valueOf(count);
            int exp = (int) (Math.log(count) / Math.log(1000));
            // Locale.US ব্যবহার করা হচ্ছে ডেসিমাল সেপারেটর হিসেবে "." নিশ্চিত করার জন্য
            return String.format(Locale.US, "%.1f%c", count / Math.pow(1000, exp), "KMBTPE".charAt(exp - 1));
        }

        // সময়কাল (সেকেন্ড) কে HH:MM:SS বা MM:SS ফরম্যাটে দেখানোর সাহায্যকারী মেথড
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