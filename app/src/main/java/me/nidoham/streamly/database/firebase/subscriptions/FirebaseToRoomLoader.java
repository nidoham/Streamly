package me.nidoham.streamly.database.firebase.subscriptions;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.schabi.newpipe.database.subscription.SubscriptionDAO;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class FirebaseToRoomLoader {
    private static final String TAG = "FirebaseToRoomLoader";
    private final Executor dbExecutor;
    private final FirebaseDatabase firebaseDatabase;
    private final SubscriptionDAO subscriptionDAO;
    private final FirebaseSyncManager firebaseSyncManager;
    private final Context context; // Added context for Toast
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // Current UTC timestamp formatter
    private static final DateTimeFormatter UTC_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public interface LoadCallback {
        void onLoadComplete(int loaded);
        void onLoadError(String error);
    }

    public FirebaseToRoomLoader(@NonNull final Context context, // Added context
                              @NonNull final SubscriptionDAO subscriptionDAO,
                              @NonNull final FirebaseSyncManager firebaseSyncManager) {
        this.context = context.getApplicationContext(); // Initialize context
        this.subscriptionDAO = subscriptionDAO;
        this.firebaseSyncManager = firebaseSyncManager;
        this.dbExecutor = Executors.newSingleThreadExecutor();
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public void loadSubscriptions(@Nullable final LoadCallback callback) {
        final String path = firebaseSyncManager.getUserSubscriptionsPath();
        if (path == null) {
            final String errorMsg = "No user identifier available for loading";
            if (callback != null) {
                callback.onLoadError(errorMsg);
            }
            return;
        }

        final String currentTime = UTC_FORMATTER.format(Instant.now());

        firebaseDatabase.getReference(path)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot snapshot) {
                    dbExecutor.execute(() -> {
                        try {
                            processFirebaseData(snapshot, callback);
                        } catch (final Exception e) {
                            final String errorMsg = "Error processing Firebase data: "
                                                    + e.getMessage();
                            if (callback != null) {
                                callback.onLoadError(errorMsg);
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull final DatabaseError error) {
                    final String errorMsg = "Firebase load cancelled: " + error.getMessage();
                    if (callback != null) {
                        callback.onLoadError(errorMsg);
                    }
                }
            });
    }

    private void processFirebaseData(@NonNull final DataSnapshot snapshot,
                                   @Nullable final LoadCallback callback) {
        try {
            final List<SubscriptionEntity> entitiesToAdd = new ArrayList<>();
            int processedCount = 0;
            for (final DataSnapshot child : snapshot.getChildren()) {
                try {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> entityMap = (Map<String, Object>) child.getValue();
                    if (entityMap != null) {
                        final SubscriptionEntity entity = firebaseSyncManager
                            .mapToEntity(entityMap);
                        if (entity != null) {
                            entitiesToAdd.add(entity);
                            processedCount++;
                        }
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "Error processing individual Firebase entry: " + child.getKey(), e);
                    // Optionally skip this entry and continue
                }
            }
            Log.d(TAG, "Parsed " + processedCount + " potential subscriptions from Firebase.");

            // --- Modification Start: Replace logic ---
            Log.d(TAG, "Clearing existing subscriptions in Room database before loading new data.");
            final int deletedRows = subscriptionDAO.deleteAll(); // Clear the table
            Log.d(TAG, "Deleted " + deletedRows + " existing subscriptions from Room.");
            // --- Modification End ---

            if (!entitiesToAdd.isEmpty()) {
                Log.d(TAG, "Inserting " + processedCount + " new subscriptions into Room.");
                // upsertAll handles potential conflicts if needed,
                // but deleteAll ensures replacement
                subscriptionDAO.upsertAll(entitiesToAdd);
                final String currentTime = UTC_FORMATTER.format(Instant.now());
                final String successMsg = String.format("Successfully loaded.",
                    processedCount, currentTime);
                Log.d(TAG, successMsg);
                showToast(processedCount + " subscriptions loaded");
                if (callback != null) {
                    callback.onLoadComplete(processedCount);
                }
            } else {
                // If Firebase has no data, the local DB is now empty after deleteAll()
                final String msg = "No subscriptions found in Firebase.";
                Log.d(TAG, msg);
                showToast("No subscriptions found in cloud");
                if (callback != null) {
                    callback.onLoadComplete(0); // Report 0 loaded
                }
            }

        } catch (final Exception e) {
            final String errorMsg = "Error during Room DB operation: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            showToast("Database error during load");
            if (callback != null) {
                callback.onLoadError(errorMsg);
            }
        }
    }

    // Helper method to show Toast on the main thread
    private void showToast(final String message) {
        mainThreadHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}
