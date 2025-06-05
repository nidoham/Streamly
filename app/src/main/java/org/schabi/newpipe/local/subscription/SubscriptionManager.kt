package org.schabi.newpipe.local.subscription

import android.content.Context
import android.util.Log
import android.util.Pair
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import me.nidoham.streamly.database.firebase.subscriptions.FirebaseSyncManager
import org.schabi.newpipe.NewPipeDatabase
import org.schabi.newpipe.database.feed.model.FeedGroupEntity
import org.schabi.newpipe.database.stream.model.StreamEntity
import org.schabi.newpipe.database.subscription.NotificationMode
import org.schabi.newpipe.database.subscription.SubscriptionDAO
import org.schabi.newpipe.database.subscription.SubscriptionEntity
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.local.feed.FeedDatabaseManager
import org.schabi.newpipe.local.feed.service.FeedUpdateInfo
import org.schabi.newpipe.util.ExtractorHelper
import org.schabi.newpipe.util.image.ImageStrategy

class SubscriptionManager(context: Context) {
    companion object {
        private const val TAG = "SubscriptionManager"
    }

    private val database = NewPipeDatabase.getInstance(context)
    private val subscriptionTable = database.subscriptionDAO()
    private val feedDatabaseManager = FeedDatabaseManager(context)
    private val firebaseSyncManager = FirebaseSyncManager(context).apply {
        setDao(subscriptionTable)
    }

    fun subscriptionTable(): SubscriptionDAO = subscriptionTable
    fun subscriptions() = subscriptionTable.all

    fun getSubscriptions(
        currentGroupId: Long = FeedGroupEntity.GROUP_ALL_ID,
        filterQuery: String = "",
        showOnlyUngrouped: Boolean = false
    ): Flowable<List<SubscriptionEntity>> {
        return when {
            filterQuery.isNotEmpty() -> {
                return if (showOnlyUngrouped) {
                    subscriptionTable.getSubscriptionsOnlyUngroupedFiltered(
                        currentGroupId, filterQuery
                    )
                } else {
                    subscriptionTable.getSubscriptionsFiltered(filterQuery)
                }
            }
            showOnlyUngrouped -> subscriptionTable.getSubscriptionsOnlyUngrouped(currentGroupId)
            else -> subscriptionTable.all
        }
    }

    fun upsertAll(infoList: List<Pair<ChannelInfo, List<ChannelTabInfo>>>): List<SubscriptionEntity> {
        val listEntities = subscriptionTable.upsertAll(
            infoList.map { SubscriptionEntity.from(it.first) }
        )

        database.runInTransaction {
            infoList.forEachIndexed { index, info ->
                info.second.forEach {
                    feedDatabaseManager.upsertAll(
                        listEntities[index].uid,
                        it.relatedItems.filterIsInstance<StreamInfoItem>()
                    )
                }
            }
        }

        // Sync new subscriptions to Firebase
        listEntities.forEach { entity ->
            firebaseSyncManager.addSubscription(entity)
        }

        return listEntities
    }

    fun updateChannelInfo(info: ChannelInfo): Completable =
        subscriptionTable.getSubscription(info.serviceId, info.url)
            .flatMapCompletable {
                Completable.fromRunnable {
                    it.setData(
                        info.name,
                        ImageStrategy.imageListToDbUrl(info.avatars),
                        info.description,
                        info.subscriberCount
                    )
                    subscriptionTable.update(it)

                    // Update in Firebase
                    firebaseSyncManager.updateSubscription(it)
                }
            }

    fun updateNotificationMode(serviceId: Int, url: String, @NotificationMode mode: Int): Completable {
        return subscriptionTable().getSubscription(serviceId, url)
            .flatMapCompletable { entity: SubscriptionEntity ->
                Completable.fromAction {
                    entity.notificationMode = mode
                    subscriptionTable().update(entity)

                    // Update in Firebase
                    firebaseSyncManager.updateSubscription(entity)
                }.apply {
                    if (mode != NotificationMode.DISABLED) {
                        // notifications have just been enabled, mark all streams as "old"
                        andThen(rememberAllStreams(entity))
                    }
                }
            }
    }

    fun updateFromInfo(info: FeedUpdateInfo) {
        val subscriptionEntity = subscriptionTable.getSubscription(info.uid)

        subscriptionEntity.name = info.name

        // some services do not provide an avatar URL
        info.avatarUrl?.let { subscriptionEntity.avatarUrl = it }

        // these two fields are null if the feed info was fetched using the fast feed method
        info.description?.let { subscriptionEntity.description = it }
        info.subscriberCount?.let { subscriptionEntity.subscriberCount = it }

        subscriptionTable.update(subscriptionEntity)

        // Update in Firebase
        firebaseSyncManager.updateSubscription(subscriptionEntity)
    }

    fun deleteSubscription(serviceId: Int, url: String): Completable {
        return Completable.fromCallable {
            subscriptionTable.deleteSubscription(serviceId, url)

            // Remove from Firebase
            firebaseSyncManager.removeSubscription(serviceId, url)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun insertSubscription(subscriptionEntity: SubscriptionEntity) {
        subscriptionTable.insert(subscriptionEntity)

        // Add to Firebase
        firebaseSyncManager.addSubscription(subscriptionEntity)
    }

    fun deleteSubscription(subscriptionEntity: SubscriptionEntity) {
        subscriptionTable.delete(subscriptionEntity)

        // Remove from Firebase
        firebaseSyncManager.removeSubscription(subscriptionEntity.serviceId, subscriptionEntity.url)
    }

    // Firebase-specific methods

    /**
     * Perform intelligent two-way sync between local database and Firebase.
     * This method will sync subscriptions in both directions, resolving conflicts intelligently.
     * * @param callback Optional callback to receive sync results
     */
    fun performFirebaseSync(callback: FirebaseSyncManager.SyncCallback? = null) {
        Log.d(TAG, "Starting Firebase sync...")
        firebaseSyncManager.performIntelligentSync(callback)
    }

    /**
     * Force upload all local subscriptions to Firebase, overwriting remote data.
     * Use this when you want to ensure Firebase has the exact same data as local storage.
     */
    fun forceUploadToFirebase() {
        Log.d(TAG, "Force uploading all subscriptions to Firebase...")
        firebaseSyncManager.forceUploadAllSubscriptions()
    }

    /**
     * Get the current subscription count from Firebase.
     * * @param onSuccess Called when count is successfully retrieved
     * @param onError Called when there's an error retrieving the count
     */
    fun getFirebaseSubscriptionCount(
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseSyncManager.getSubscriptionCount(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val count = snapshot.getValue(Long::class.java) ?: 0L
                onSuccess(count)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                onError(error.message)
            }
        })
    }

    /**
     * Enable or disable automatic Firebase sync for subscription operations.
     * When enabled, all local operations will automatically sync to Firebase.
     * * @param enabled Whether to enable automatic sync
     */
    fun setFirebaseAutoSyncEnabled(enabled: Boolean) {
        // This could be implemented with a shared preference or similar mechanism
        // For now, it's always enabled by default
        Log.d(TAG, "Firebase auto-sync ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Check if the user is authenticated with Firebase.
     * * @return true if user is logged in, false otherwise
     */
    fun isFirebaseAuthenticated(): Boolean {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Get Firebase sync manager for advanced operations.
     * Use this if you need to perform custom Firebase operations not covered by this class.
     * * @return The Firebase sync manager instance
     */
    fun getFirebaseSyncManager(): FirebaseSyncManager = firebaseSyncManager

    /**
     * Perform a complete sync with detailed callback information.
     * This is useful for UI updates and showing sync progress to users.
     * * @param onStart Called when sync begins
     * @param onProgress Called during sync with progress updates (if available)
     * @param onComplete Called when sync completes successfully
     * @param onError Called if sync fails
     */
    fun performDetailedFirebaseSync(
        onStart: () -> Unit = {},
        onProgress: (String) -> Unit = {},
        onComplete: (added: Int, updated: Int, removed: Int) -> Unit = { _, _, _ -> },
        onError: (String) -> Unit = {}
    ) {
        onStart()
        onProgress("Starting Firebase sync...")

        firebaseSyncManager.performIntelligentSync(object : FirebaseSyncManager.SyncCallback {
            override fun onSyncComplete(added: Int, updated: Int, removed: Int) {
                Log.d(TAG, "Firebase sync completed: $added added, $updated updated, $removed removed")
                onComplete(added, updated, removed)
            }

            override fun onSyncError(error: String) {
                Log.e(TAG, "Firebase sync failed: $error")
                onError(error)
            }
        })
    }

    /**
     * Fetches the list of videos for the provided channel and saves them in the database, so that
     * they will be considered as "old"/"already seen" streams and the user will never be notified
     * about any one of them.
     */
    private fun rememberAllStreams(subscription: SubscriptionEntity): Completable {
        return ExtractorHelper.getChannelInfo(subscription.serviceId, subscription.url, false)
            .flatMap { info ->
                ExtractorHelper.getChannelTab(subscription.serviceId, info.tabs.first(), false)
            }
            .map { channel -> channel.relatedItems.filterIsInstance<StreamInfoItem>().map { stream -> StreamEntity(stream) } }
            .flatMapCompletable { entities ->
                Completable.fromAction {
                    database.streamDAO().upsertAll(entities)
                }
            }.onErrorComplete()
    }
}
