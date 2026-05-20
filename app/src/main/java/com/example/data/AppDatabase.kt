package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Reels queries
    @Query("SELECT * FROM reels ORDER BY timestamp DESC")
    fun getAllReels(): Flow<List<ReelItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelItem)

    @Update
    suspend fun updateReel(reel: ReelItem)

    @Query("DELETE FROM reels WHERE id = :id")
    suspend fun deleteReel(id: Int)

    @Query("SELECT * FROM reels WHERE id = :id")
    suspend fun getReelById(id: Int): ReelItem?

    // Comments queries
    @Query("SELECT * FROM comments WHERE reelId = :reelId ORDER BY timestamp ASC")
    fun getCommentsForReel(reelId: Int): Flow<List<CommentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentItem)

    // Notifications queries
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    // Profile queries
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}

@Database(
    entities = [ReelItem::class, CommentItem::class, NotificationItem::class, UserProfile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}
