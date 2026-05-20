package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ReelItem
import com.example.data.UserProfile
import com.example.ui.AppViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val profileOpt by viewModel.profile.collectAsState()
    val reels by viewModel.reels.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val profile = profileOpt ?: UserProfile(
        id = 1,
        name = "Vicky Neon",
        username = "vicky_neon",
        avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop",
        bio = "Loading digital avatar system... Setup Complete.",
        followersCount = 4250,
        followingCount = 184
    )

    // Filter reels created by this user
    val userReels = reels.filter { it.creatorName == profile.username }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCosmos)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Offset for bottom navigation bar
        ) {
            // Header Image Cover Grid-breaker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricIndigo, HotPink)
                        )
                    )
            ) {
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Cyber Details",
                        tint = NeonBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Profile Info content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar with offset breaking grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-45).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .border(BorderStroke(3.dp, NeonBlue), CircleShape)
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = profile.avatar,
                            contentDescription = "Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    // Followers / Following layout counters
                    Row(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatColumn(count = "${userReels.size}", label = "Posts")
                        ProfileStatColumn(count = formatNumber(profile.followersCount), label = "Followers")
                        ProfileStatColumn(count = formatNumber(profile.followingCount), label = "Following")
                    }
                }

                // Profile details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-38).dp)
                ) {
                    Text(
                        text = profile.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "@${profile.username}",
                        color = NeonBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.bio,
                        color = OffWhite,
                        fontSize = 12.sp,
                        maxLines = 3,
                        lineHeight = 16.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Grid Section block header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(vertical = 10.dp, horizontal = 16.dp)
                    .border(BorderStroke(0.5.dp, DarkStroke), RoundedCornerShape(4.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reel grid icon",
                        tint = NeonBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "COMPILED NODES (${userReels.size})",
                        color = NeonBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Scrollable Grid views
            if (userReels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyberGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No shorts compiled yet.",
                            color = CyberGray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .offset(y = (-20).dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(userReels, key = { it.id }) { reel ->
                        ProfileVideoTile(reel)
                    }
                }
            }
        }

        // Edit Profile Dialog dialog box
        if (showEditDialog) {
            EditProfileDialog(
                currentProfile = profile,
                onSave = { name, username, bio ->
                    viewModel.updateProfile(name, username, bio)
                    showEditDialog = false
                },
                onDismiss = { showEditDialog = false }
            )
        }
    }
}

@Composable
fun ProfileStatColumn(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = CyberGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileVideoTile(reel: ReelItem) {
    // Generate simple aesthetic thumb by matching looping sequence
    val previewThumbUrl = when {
        reel.videoUrl.contains("44131") -> "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?w=150&auto=format&fit=crop"
        reel.videoUrl.contains("44133") -> "https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=150&auto=format&fit=crop"
        reel.videoUrl.contains("44135") -> "https://images.unsplash.com/photo-1518770660439-4636190af475?w=150&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=150&auto=format&fit=crop"
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(4.dp))
            .clickable { /* Feed plays it */ }
    ) {
        AsyncImage(
            model = previewThumbUrl,
            contentDescription = reel.description,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Mini play stats
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 0f
                    )
                )
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${reel.likesCount}",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.name) }
    var username by remember { mutableStateOf(currentProfile.username) }
    var bio by remember { mutableStateOf(currentProfile.bio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepCosmos,
        modifier = Modifier.border(BorderStroke(1.dp, DarkStroke), RoundedCornerShape(12.dp)),
        title = {
            Text(
                text = "RECOMPILE DIGITAL AVATAR",
                color = NeonBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", color = CyberGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name"),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Cyber Username", color = CyberGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_username"),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Holographic Bio", color = CyberGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("edit_profile_bio"),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, username, bio) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text(text = "RECOMPILE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_profile_button")) {
                Text(text = "CANCEL", color = CyberGray, fontWeight = FontWeight.Bold)
            }
        }
    )
}

fun formatNumber(num: Int): String {
    return if (num >= 1000) {
        "${String.format("%.1f", num / 1000f)}k"
    } else {
        "$num"
    }
}
