package com.example.ui.screens

import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.CommentItem
import com.example.data.ReelItem
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReelsFeedScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val reels by viewModel.reels.collectAsState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var activeIndex by remember { mutableStateOf(0) }
    var showCommentsSheetForReelId by remember { mutableStateOf<Int?>(null) }
    var showShareSheetForReel by remember { mutableStateOf<ReelItem?>(null) }
    var showSponsoredAdUrl by remember { mutableStateOf<ReelItem?>(null) }

    // Synchronize current playing reel index with scrolling
    LaunchedEffect(lazyListState.firstVisibleItemIndex) {
        activeIndex = lazyListState.firstVisibleItemIndex
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCosmos)
    ) {
        if (reels.isEmpty()) {
            // Seeding loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "COMPILING FEED ALGORITHMS...",
                        color = NeonBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(reels, key = { _, reel -> reel.id }) { index, reel ->
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .background(Color.Black)
                    ) {
                        // Embedded Video Player
                        VideoPlayer(
                            videoUrl = reel.videoUrl,
                            isPlaying = (index == activeIndex && showCommentsSheetForReelId == null),
                            modifier = Modifier.fillMaxSize()
                        )

                        // 3D Glassmorphic Bottom Blur Overlay to isolate visual inputs
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                        startY = 0f
                                    )
                                )
                        )

                        // Double tap heart Sparks layer
                        var sparkTrigger by remember { mutableStateOf(false) }
                        var sparkOffset by remember { mutableStateOf(Offset.Zero) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { offset ->
                                            sparkOffset = offset
                                            sparkTrigger = true
                                            if (!reel.isLiked) {
                                                viewModel.toggleLike(reel.id)
                                            }
                                        }
                                    )
                                }
                        )

                        if (sparkTrigger) {
                            DoubleTapSpark(
                                offset = sparkOffset,
                                onAnimationEnd = { sparkTrigger = false }
                            )
                        }

                        // Right Sidebar for interactions
                        SidebarOverlay(
                            reel = reel,
                            onLikeToggle = { viewModel.toggleLike(reel.id) },
                            onFollowToggle = { viewModel.toggleFollow(reel.id) },
                            onCommentsOpen = { showCommentsSheetForReelId = reel.id },
                            onShareOpen = { showShareSheetForReel = reel },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp, bottom = 80.dp)
                        )

                        // Bottom caption details
                        CaptionOverlay(
                            reel = reel,
                            onSponsorClick = { showSponsoredAdUrl = reel },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 16.dp, end = 90.dp, bottom = 96.dp)
                        )
                    }
                }
            }
        }

        // Action Sheets
        showCommentsSheetForReelId?.let { reelId ->
            CommentsBottomSheet(
                reelId = reelId,
                viewModel = viewModel,
                onDismiss = { showCommentsSheetForReelId = null }
            )
        }

        showShareSheetForReel?.let { reel ->
            ShareBottomSheet(
                reel = reel,
                onDismiss = { showShareSheetForReel = null }
            )
        }

        showSponsoredAdUrl?.let { sponsorReel ->
            HolographicBrowserOverlay(
                reel = sponsorReel,
                onDismiss = { showSponsoredAdUrl = null }
            )
        }
    }
}

@Composable
fun VideoPlayer(
    videoUrl: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    try {
                        mp.setVolume(0.4f, 0.4f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (isPlaying) {
                        start()
                    }
                }
                setVideoPath(videoUrl)
                videoViewRef = this
            }
        },
        update = { view ->
            try {
                if (isPlaying) {
                    if (!view.isPlaying) {
                        view.start()
                    }
                } else {
                    if (view.isPlaying) {
                        view.pause()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        modifier = modifier
    )

    DisposableEffect(videoUrl) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun DoubleTapSpark(
    offset: Offset,
    onAnimationEnd: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val rotate = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 2.4f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            rotate.animateTo(35f, tween(300))
        }
        delay(400)
        launch {
            alpha.animateTo(0f, tween(200))
        }
        delay(200)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .offset(
                x = (offset.x / 2.5f).dp,
                y = (offset.y / 2.5f).dp
            )
            .size(72.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Double Tap Spark",
            tint = HotPink,
            modifier = Modifier
                .fillMaxSize()
                .drawWithNeonGlow(HotPink)
                .align(Alignment.Center)
        )
    }
}

// Neon Glow modifier helper
fun Modifier.drawWithNeonGlow(color: Color): Modifier = this.drawBehind {
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = size.minDimension / 1.5f,
        center = center
    )
    drawCircle(
        color = color.copy(alpha = 0.15f),
        radius = size.minDimension * 1.1f,
        center = center
    )
}

@Composable
fun SidebarOverlay(
    reel: ReelItem,
    onLikeToggle: () -> Unit,
    onFollowToggle: () -> Unit,
    onCommentsOpen: () -> Unit,
    onShareOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Creator profile avatar with holographic neon border ring
        Box(
            modifier = Modifier
                .size(54.dp)
                .clickable { onFollowToggle() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        BorderStroke(
                            2.dp,
                            if (reel.isFollowing) ElectricIndigo else NeonBlue
                        ), CircleShape
                    )
                    .padding(3.dp)
            ) {
                AsyncImage(
                    model = reel.creatorAvatar,
                    contentDescription = "Creator Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            // Follow indicator mini badge plus symbol
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
                    .background(
                        if (reel.isFollowing) ElectricIndigo else NeonBlue,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (reel.isFollowing) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = "Follow State",
                    tint = Color.Black,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Custom Glassmorphic buttons for Like, Comments, Share
        SidebarGlassButton(
            icon = Icons.Filled.Favorite,
            badge = if (reel.likesCount > 1000) "${String.format("%.1f", reel.likesCount / 1000f)}k" else "${reel.likesCount}",
            tint = if (reel.isLiked) HotPink else Color.White,
            testTag = "like_button_${reel.id}",
            onClick = onLikeToggle
        )

        SidebarGlassButton(
            icon = Icons.Filled.Comment,
            badge = "${reel.commentsCount}",
            tint = NeonBlue,
            testTag = "comments_button_${reel.id}",
            onClick = onCommentsOpen
        )

        SidebarGlassButton(
            icon = Icons.Filled.Share,
            badge = "Share",
            tint = ElectricIndigo,
            testTag = "share_button_${reel.id}",
            onClick = onShareOpen
        )
    }
}

@Composable
fun SidebarGlassButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall.copy(
                shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 4f)
            )
        )
    }
}

@Composable
fun CaptionOverlay(
    reel: ReelItem,
    onSponsorClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Handle name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (reel.isSponsored) "SPONSORED PROMO" else "@${reel.creatorName}",
                color = if (reel.isSponsored) HotPink else Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 3f)
                )
            )
            // Premium verified badge indicator
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (reel.isSponsored) HotPink else NeonBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (reel.isSponsored) Icons.Filled.Star else Icons.Filled.Check,
                    contentDescription = "Verified Profile",
                    tint = Color.Black,
                    modifier = Modifier.size(9.dp)
                )
            }

            if (reel.isSponsored) {
                Box(
                    modifier = Modifier
                        .background(HotPink.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(BorderStroke(1.dp, HotPink), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD",
                        color = HotPink,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Full caption body
        Text(
            text = reel.description,
            color = OffWhite,
            fontSize = 13.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 3f)
            )
        )

        if (reel.isSponsored && reel.sponsorActionText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSponsorClick,
                colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("sponsored_action_button_${reel.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = reel.sponsorActionText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Rotating original music track component with glass backdrop
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(BorderStroke(0.5.dp, DarkStroke), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Track Music",
                tint = NeonBlue,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = reel.audioName,
                color = NeonBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    reelId: Int,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val commentsState by viewModel.getComments(reelId).collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepCosmos,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = CyberGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title neon grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECOMPILE RESPONSES (${commentsState.size})",
                    color = NeonBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = CyberGray)
                }
            }

            Divider(thickness = 0.5.dp, color = DarkStroke)

            // Dynamic comments scroll view
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (commentsState.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Filled.Message, contentDescription = null, tint = LightStroke, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No frequencies detected. Be first!", color = CyberGray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(commentsState.size) { index ->
                            val comment = commentsState[index]
                            CommentItemRow(comment)
                        }
                    }
                }
            }

            Divider(thickness = 0.5.dp, color = DarkStroke)

            // Neon Blue inputs block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Compile response...", color = CyberGray, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = OffWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 2
                )

                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(reelId, commentText)
                            commentText = ""
                            // Scroll down as new comments enter stream
                            scope.launch {
                                delay(150)
                                if (commentsState.isNotEmpty()) {
                                    listState.animateScrollToItem(commentsState.size - 1)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(NeonBlue, CircleShape)
                        .border(BorderStroke(1.dp, Color.White), CircleShape)
                        .testTag("send_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Comment",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItemRow(comment: CommentItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = comment.userAvatar,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, DarkStroke), CircleShape)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "@${comment.userName}",
                    color = NeonBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• verified node",
                    color = ElectricIndigo.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.text,
                color = OffWhite,
                fontSize = 13.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    reel: ReelItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isLinkCopied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepCosmos,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CyberGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp, top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRANSMIT FREQUENCY",
                color = ElectricIndigo,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Copy secure stream node logic to clipboards below:",
                color = CyberGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Neon Box containing mock node url
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, DarkStroke), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "https://sspreels.aistudio/node-${reel.id}",
                    color = NeonBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { isLinkCopied = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLinkCopied) ElectricIndigo else NeonBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isLinkCopied) "COPIED" else "COPY",
                        color = if (isLinkCopied) Color.White else Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action lists
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ShareAppIconItem(icon = Icons.Filled.Send, label = "Instantly", tint = NeonBlue)
                ShareAppIconItem(icon = Icons.Filled.Mail, label = "Email", tint = ElectricIndigo)
                ShareAppIconItem(icon = Icons.Filled.QrCode, label = "QR Code", tint = HotPink)
                ShareAppIconItem(icon = Icons.Filled.Chat, label = "SMS link", tint = Color.White)
            }
        }
    }
}

@Composable
fun ShareAppIconItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {}
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(tint.copy(alpha = 0.1f), CircleShape)
                .border(BorderStroke(1.dp, tint.copy(alpha = 0.25f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = CyberGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HolographicBrowserOverlay(
    reel: ReelItem,
    onDismiss: () -> Unit
) {
    var isMobileMode by remember { mutableStateOf(false) }
    var adClickCount by remember { mutableStateOf(0) }
    var hoveredCardId by remember { mutableStateOf<Int?>(null) }
    var scanlineAnimation by remember { mutableStateOf(true) }

    // Automatic scanline pulse effect
    LaunchedEffect(scanlineAnimation) {
        if (scanlineAnimation) {
            delay(1200)
            scanlineAnimation = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .border(BorderStroke(2.dp, Brush.linearGradient(listOf(NeonBlue, HotPink))), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DeepCosmos),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left window circles design
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(10.dp).background(HotPink, CircleShape))
                        Box(modifier = Modifier.size(10.dp).background(Color.Yellow, CircleShape))
                        Box(modifier = Modifier.size(10.dp).background(NeonBlue, CircleShape))
                    }

                    // Responsive toggle buttons
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(12.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isMobileMode = false },
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (!isMobileMode) NeonBlue.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = "Desktop view mode",
                                tint = if (!isMobileMode) NeonBlue else CyberGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { isMobileMode = true },
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (isMobileMode) HotPink.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = "Mobile view mode",
                                tint = if (isMobileMode) HotPink else CyberGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Exit action
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Simulated Web ad",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // address bar URL simulator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "SSL Secured Connection",
                            tint = Color.Green,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reel.sponsorUrl,
                            color = CyberGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clicks: $adClickCount",
                        color = HotPink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .background(HotPink.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, HotPink.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                // Simulated Content Web Engine with responsive scales
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val browserWidthModifier = if (isMobileMode) {
                        Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    }

                    Box(
                        modifier = browserWidthModifier
                            .background(Color(0xFFF4F4F4))
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            // 1. Navigation Bar
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2C3E50))
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Home", "Gallery", "About", "Contact").forEach { navLink ->
                                        Text(
                                            text = navLink,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable { adClickCount++ }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // 2. Hero banner element
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF3498DB))
                                        .padding(horizontal = 16.dp, vertical = 26.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Welcome to My Responsive Website",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        lineHeight = 22.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Text(
                                        text = "Yahan images ka size container ke hisaab se automatic change hota hai.",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    )
                                }
                            }

                            // 3. Grid demonstration container
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Image Size Control Demo",
                                        color = Color(0xFF333333),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(bottom = 16.dp)
                                    )

                                    if (isMobileMode) {
                                        // Mobile View: All elements are scaled 100% full-width stacked
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            SimulatedHtmlAdCard(
                                                title = "Small (200px -> styled 100%)",
                                                subtitle = "Perfect for micro devices",
                                                imageWidthDp = -1, // representing 100% full width
                                                cardId = 1,
                                                hoveredCardId = hoveredCardId,
                                                onHover = { hoveredCardId = it },
                                                onClick = { adClickCount++ }
                                            )
                                            SimulatedHtmlAdCard(
                                                title = "Medium (350px -> styled 100%)",
                                                subtitle = "Scales adaptively on smartphones",
                                                imageWidthDp = -1,
                                                cardId = 2,
                                                hoveredCardId = hoveredCardId,
                                                onHover = { hoveredCardId = it },
                                                onClick = { adClickCount++ }
                                            )
                                            SimulatedHtmlAdCard(
                                                title = "Large (Full Width)",
                                                subtitle = "Standard display matrix container",
                                                imageWidthDp = -1,
                                                cardId = 3,
                                                hoveredCardId = hoveredCardId,
                                                onHover = { hoveredCardId = it },
                                                onClick = { adClickCount++ }
                                            )
                                        }
                                    } else {
                                        // Desktop View: Responsive grid structure (Flow style side-by-side or stacked proportionately)
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(modifier = Modifier.weight(1.2f)) {
                                                    SimulatedHtmlAdCard(
                                                        title = "Small (200px)",
                                                        subtitle = "Clean sidebar node",
                                                        imageWidthDp = 200,
                                                        cardId = 1,
                                                        hoveredCardId = hoveredCardId,
                                                        onHover = { hoveredCardId = it },
                                                        onClick = { adClickCount++ }
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1.8f)) {
                                                    SimulatedHtmlAdCard(
                                                        title = "Medium (350px)",
                                                        subtitle = "Medium content layout banner",
                                                        imageWidthDp = 350,
                                                        cardId = 2,
                                                        hoveredCardId = hoveredCardId,
                                                        onHover = { hoveredCardId = it },
                                                        onClick = { adClickCount++ }
                                                    )
                                                }
                                            }

                                            SimulatedHtmlAdCard(
                                                title = "Large (Full Width)",
                                                subtitle = "Expanded hero size banner",
                                                imageWidthDp = -1, // Full width large box
                                                cardId = 3,
                                                hoveredCardId = hoveredCardId,
                                                onHover = { hoveredCardId = it },
                                                onClick = { adClickCount++ }
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Simulated Footer
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2C3E50))
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "© 2026 My Website Design | Made with ❤️",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Interactive scanline pulse loading overlay simulation
                    if (scanlineAnimation) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NeonBlue.copy(alpha = 0.05f))
                        ) {
                            Text(
                                text = "SIMULATING CHROME RENDER...",
                                color = NeonBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedHtmlAdCard(
    title: String,
    subtitle: String,
    imageWidthDp: Int, // if -1, represent full container width (100%)
    cardId: Int,
    hoveredCardId: Int?,
    onHover: (Int?) -> Unit,
    onClick: () -> Unit
) {
    val isHovered = hoveredCardId == cardId
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = Modifier
            .then(
                if (imageWidthDp > 0) Modifier.width(imageWidthDp.dp) else Modifier.fillMaxWidth()
            )
            .clickable {
                onHover(cardId)
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Simulated local static responsive web page graphics with double gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (imageWidthDp == 200) 80.dp else 120.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3498DB),
                                Color(0xFFE74C3C),
                                Color(0xFF2ECC71)
                            ).shuffled()
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Image graphic icon mockup
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

val LightStroke = Color(0x11FFFFFF)

