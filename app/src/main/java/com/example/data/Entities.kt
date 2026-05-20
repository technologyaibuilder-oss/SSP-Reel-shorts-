package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reels")
data class ReelItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoUrl: String,
    val creatorName: String,
    val creatorAvatar: String,
    val description: String,
    val likesCount: Int,
    val commentsCount: Int,
    val isLiked: Boolean = false,
    val isFollowing: Boolean = false,
    val audioName: String = "Original Audio",
    val timestamp: Long = System.currentTimeMillis(),
    val isSponsored: Boolean = false,
    val sponsorActionText: String = "",
    val sponsorUrl: String = ""
)

@Entity(tableName = "comments")
data class CommentItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reelId: Int,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val username: String,
    val avatar: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int
)
