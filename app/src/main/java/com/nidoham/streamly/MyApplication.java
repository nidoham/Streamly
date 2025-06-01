package com.nidoham.streamly; // Replace with your actual package name

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.nidoham.streamly.downloader.DownloaderImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeNewPipeExtractor();
    }

    private void initializeNewPipeExtractor() {
        Log.d(TAG, "Initializing NewPipeExtractor...");
        final Context context = this;

        // Initialize the downloader. You might want to customize the OkHttpClient.
        // DownloaderImpl.init(null); // Pass a custom OkHttpClient if needed, null uses default.
        final Downloader downloader = DownloaderImpl.init(null);

        // Initialize NewPipeExtractor with the downloader
        NewPipe.init(downloader);
        Log.d(TAG, "NewPipeExtractor initialized successfully.");
    }
}
