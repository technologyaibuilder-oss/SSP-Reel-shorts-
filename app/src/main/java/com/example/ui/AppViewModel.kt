package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CommentItem
import com.example.data.DatabaseProvider
import com.example.data.NotificationItem
import com.example.data.ReelItem
import com.example.data.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatabaseProvider.getRepository(application)

    // State flows
    val reels: StateFlow<List<ReelItem>> = repository.allReels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated content creation upload state
    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress: StateFlow<Float?> = _uploadProgress.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Comments Flow for a specific Reel ID
    fun getComments(reelId: Int): Flow<List<CommentItem>> {
        return repository.getCommentsForReel(reelId)
    }

    // Like Action
    fun toggleLike(reelId: Int) {
        viewModelScope.launch {
            repository.toggleLike(reelId)
        }
    }

    // Follow Action
    fun toggleFollow(reelId: Int) {
        viewModelScope.launch {
            repository.toggleFollow(reelId)
        }
    }

    // Add Comment
    fun addComment(reelId: Int, commentText: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            val user = profile.value
            val userName = user?.username ?: "cyber_guest"
            val userAvatar = user?.avatar ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop"
            val comment = CommentItem(
                reelId = reelId,
                userName = userName,
                userAvatar = userAvatar,
                text = commentText,
                timestamp = System.currentTimeMillis()
            )
            repository.addComment(comment)
        }
    }

    // Update Profile
    fun updateProfile(name: String, username: String, bio: String) {
        viewModelScope.launch {
            val current = profile.value
            val updated = current?.copy(
                name = name,
                username = username,
                bio = bio
            ) ?: UserProfile(
                id = 1,
                name = name,
                username = username,
                avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop",
                bio = bio,
                followersCount = 4250,
                followingCount = 184
            )
            repository.saveProfile(updated)
            repository.addNotification(
                "👤 Profile Updated",
                "Your cyber identity details have been recompiled successfully."
            )
        }
    }

    // Simulated Reel uploads. We use beautiful Mixkit loops as default mock videos
    fun simulateVideoUpload(description: String, videoIndex: Int) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadProgress.value = 0.0f
            
            // Increment progress over a couple of seconds to make it satisfying
            val steps = 10
            for (i in 1..steps) {
                delay(250)
                _uploadProgress.value = i.toFloat() / steps.toFloat()
            }

            // High-quality loop choices for user uploads
            val videoUrls = listOf(
                "https://assets.mixkit.co/videos/preview/mixkit-cyberpunk-neon-city-street-at-night-44131-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-futuristic-subway-station-with-neon-lights-44133-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-flying-through-a-futuristic-tunnel-with-neon-lights-44135-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-lens-flares-of-futuristic-blue-neon-lights-44129-large.mp4"
            )

            val selectedUrl = videoUrls[videoIndex.coerceIn(0, videoUrls.lastIndex)]
            val user = profile.value
            val creatorName = user?.username ?: "vicky_neon"
            val creatorAvatar = user?.avatar ?: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&auto=format&fit=crop"

            val newReel = ReelItem(
                videoUrl = selectedUrl,
                creatorName = creatorName,
                creatorAvatar = creatorAvatar,
                description = description.ifBlank { "Unleashing neon codes in the digital realm. ⚡ #creation #SSPReel" },
                likesCount = 0,
                commentsCount = 0,
                isLiked = false,
                isFollowing = false,
                audioName = "SSPSynth Grid Studio - Original Sound",
                timestamp = System.currentTimeMillis()
            )

            repository.insertReel(newReel)
            
            repository.addNotification(
                "🎬 Short Uploaded!",
                "Your short video \"${newReel.description.take(20)}...\" was compiled and listed."
            )

            _uploadProgress.value = null
            _isUploading.value = false
        }
    }
}

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
