package com.example.data

import com.example.data.AppDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val dao: AppDao) {

    val allReels: Flow<List<ReelItem>> = dao.getAllReels()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allNotifications: Flow<List<NotificationItem>> = dao.getNotifications()

    fun getCommentsForReel(reelId: Int): Flow<List<CommentItem>> {
        return dao.getCommentsForReel(reelId)
    }

    suspend fun insertReel(reelItem: ReelItem) {
        dao.insertReel(reelItem)
    }

    suspend fun updateReel(reelItem: ReelItem) {
        dao.updateReel(reelItem)
    }

    suspend fun deleteReel(id: Int) {
        dao.deleteReel(id)
    }

    suspend fun addComment(commentItem: CommentItem) {
        dao.insertComment(commentItem)
        // Also increment commentsCount on the reel itself
        val reel = dao.getReelById(commentItem.reelId)
        if (reel != null) {
            dao.updateReel(reel.copy(commentsCount = reel.commentsCount + 1))
        }
    }

    suspend fun toggleLike(reelId: Int) {
        val reel = dao.getReelById(reelId)
        if (reel != null) {
            val updatedLiked = !reel.isLiked
            val delta = if (updatedLiked) 1 else -1
            val updatedLikesCount = (reel.likesCount + delta).coerceAtLeast(0)
            dao.updateReel(reel.copy(isLiked = updatedLiked, likesCount = updatedLikesCount))

            // Add notification if liked
            if (updatedLiked) {
                dao.insertNotification(
                    NotificationItem(
                        title = "❤️ Reel Liked",
                        body = "Someone liked your short: \"${reel.description.take(20)}...\"",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun toggleFollow(reelId: Int) {
        val reel = dao.getReelById(reelId)
        if (reel != null) {
            val updatedFollow = !reel.isFollowing
            dao.updateReel(reel.copy(isFollowing = updatedFollow))

            // Add notification of follow/unfollow
            dao.insertNotification(
                NotificationItem(
                    title = if (updatedFollow) "👤 New Follower" else "👤 Unfollowed",
                    body = if (updatedFollow) {
                        "You are now following @${reel.creatorName}!"
                    } else {
                        "You unfollowed @${reel.creatorName}."
                    },
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun addNotification(title: String, body: String) {
        dao.insertNotification(NotificationItem(title = title, body = body))
    }

    suspend fun seedDatabaseIfEmpty() {
        val currentReels = dao.getAllReels().firstOrNull() ?: emptyList()
        if (currentReels.isEmpty()) {
            val defaultReels = listOf(
                ReelItem(
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-cyberpunk-neon-city-street-at-night-44131-large.mp4",
                    creatorName = "neon_shibuya",
                    creatorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&auto=format&fit=crop",
                    description = "Exploring Shibuya in 2077. The neon rain hits different. 🌧️🎮 #cyberpunk #neon #tokyo",
                    likesCount = 5824,
                    commentsCount = 2,
                    isLiked = false,
                    isFollowing = false,
                    audioName = "Lofi Shibuya Beats - Synthwave Chill"
                ),
                ReelItem(
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-lens-flares-of-futuristic-blue-neon-lights-44129-large.mp4",
                    creatorName = "neural_mesh_ads",
                    creatorAvatar = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=120&auto=format&fit=crop",
                    description = "🚀 HOLOGRAPHIC CHIPS INJECTED. Upgrade your neural cybernetics with 60% OFF today! Click below to recompile your vision sensor module. 📲⚡ #ads #sponsored #future #neural",
                    likesCount = 1248,
                    commentsCount = 1,
                    isLiked = false,
                    isFollowing = false,
                    audioName = "Ad Resonance - Neural Grid Synth",
                    isSponsored = true,
                    sponsorActionText = "UPGRADE PROTOCOL",
                    sponsorUrl = "https://syntheticgrid.aistudio/neuralwear-upgrade"
                ),
                ReelItem(
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-futuristic-subway-station-with-neon-lights-44133-large.mp4",
                    creatorName = "retro_grid",
                    creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&auto=format&fit=crop",
                    description = "Just catching the last train home in Metro Sector 9. 🚅🌌 #futuristic #synthwave #metro",
                    likesCount = 4120,
                    commentsCount = 1,
                    isLiked = true,
                    isFollowing = true,
                    audioName = "Subway Resonance Synth - Retro SciFi"
                ),
                ReelItem(
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-cyberpunk-neon-city-street-at-night-44131-large.mp4",
                    creatorName = "nexus_cyberwear",
                    creatorAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120&auto=format&fit=crop",
                    description = "🔥 APEX LED NEON JACKETS. Imbued with active 3D glassmorphic temperature sensors. Wear the future or stay in the dark. Use SSP50 discount coupon now! 🧥💎 #sponsored #apparel #ads",
                    likesCount = 2095,
                    commentsCount = 1,
                    isLiked = false,
                    isFollowing = false,
                    audioName = "Sponsor beats - Elite Threads Loops",
                    isSponsored = true,
                    sponsorActionText = "SHOP DESIGN CODES",
                    sponsorUrl = "https://cyberstyle.aistudio/nexus-led-jacket"
                ),
                ReelItem(
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-flying-through-a-futuristic-tunnel-with-neon-lights-44135-large.mp4",
                    creatorName = "grid_runner",
                    creatorAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120&auto=format&fit=crop",
                    description = "Flying through the Grid Sector 0! Infinite velocity. 🚀✨ #neonblue #tunnel #gaming",
                    likesCount = 9439,
                    commentsCount = 3,
                    isLiked = false,
                    isFollowing = false,
                    audioName = "Laser Grid Symphony - Vol 2"
                ),
                ReelItem(
                    videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-lens-flares-of-futuristic-blue-neon-lights-44129-large.mp4",
                    creatorName = "synth_flare",
                    creatorAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120&auto=format&fit=crop",
                    description = "Sleek lens flares from high-voltage neon grids. Super satisfying loops. ⚡💙 #lensflare #neonlighting",
                    likesCount = 2392,
                    commentsCount = 1,
                    isLiked = false,
                    isFollowing = false,
                    audioName = "High-Voltage Ambient Synth Loop"
                )
            )
            for (reel in defaultReels) {
                dao.insertReel(reel)
            }

            // Seed some comments
            val inserted = dao.getAllReels().first()
            if (inserted.isNotEmpty()) {
                dao.insertComment(CommentItem(reelId = inserted[0].id, userName = "cyber_shawn", userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop", text = "This is literally insane! The graphics 💖"))
                dao.insertComment(CommentItem(reelId = inserted[0].id, userName = "glitch_girl", userAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&auto=format&fit=crop", text = "Pure 2077 vibes, retro style!"))
                
                dao.insertComment(CommentItem(reelId = inserted[1].id, userName = "synth_king", userAvatar = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&auto=format&fit=crop", text = "The audio track is absolutely beautiful."))
                
                dao.insertComment(CommentItem(reelId = inserted[2].id, userName = "tron_runner", userAvatar = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=100&auto=format&fit=crop", text = "Reminds me of Tron Legacy! Excellent!"))
                dao.insertComment(CommentItem(reelId = inserted[2].id, userName = "neon_rider", userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop", text = "Keep compiling these loops!"))
                dao.insertComment(CommentItem(reelId = inserted[2].id, userName = "pixel_perfect", userAvatar = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100&auto=format&fit=crop", text = "Love the glassmorphic overlay details as well"))
                
                dao.insertComment(CommentItem(reelId = inserted[3].id, userName = "electric_mind", userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&auto=format&fit=crop", text = "Wow! Simply wow."))
            }

            // Seed user profile
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Vicky Neon",
                    username = "vicky_neon",
                    avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop",
                    bio = "Building the futuristic SSP Reel shorts app. 3D glassmorphic designs and neon vibes always. ✨🚀 #indiedev",
                    followersCount = 4250,
                    followingCount = 184
                )
            )

            // Seed default notifications
            dao.insertNotification(
                NotificationItem(
                    title = "✨ Welcome to SSP Reel shorts",
                    body = "Step into the future. Tap double-tap on any video to show neon spark heart effects! Enjoy copyright-free loops in ultra speed.",
                    timestamp = System.currentTimeMillis()
                )
            )
            dao.insertNotification(
                NotificationItem(
                    title = "🚀 App Initialized",
                    body = "Neon aesthetic initialized smoothly. Standard database tables setup in offline-first mode.",
                    timestamp = System.currentTimeMillis() - 60000
                )
            )
        }
    }
}
