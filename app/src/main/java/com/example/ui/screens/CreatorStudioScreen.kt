package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorStudioScreen(
    viewModel: AppViewModel,
    onUploadSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var captionText by remember { mutableStateOf("") }
    var selectedVideoIndex by remember { mutableStateOf(0) }
    
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Interactive logs for compile simulation text updates
    var compileStatusString by remember { mutableStateOf("Ready to capture stream data") }

    LaunchedEffect(isUploading, uploadProgress) {
        if (isUploading && uploadProgress != null) {
            val progress = uploadProgress!!
            compileStatusString = when {
                progress < 0.2f -> "Initializing video codecs & audio grids..."
                progress < 0.4f -> "Injecting Dolby-Synthesizer soundtrack..."
                progress < 0.6f -> "Injecting glassmorphic overlay metadata..."
                progress < 0.8f -> "Distributing secure HLS fragment nodes..."
                else -> "Compiling SSP stream node binary... almost done!"
            }
        } else if (!isUploading) {
            compileStatusString = "Broadcasting stream successfully closed."
        }
    }

    // Static available looping feeds references
    val mockThumbnails = listOf(
        Pair("Shibuya Neon Night", "https://images.unsplash.com/photo-1540959733332-eab4deceeaf7?w=300&auto=format&fit=crop"),
        Pair("Futuristic Station", "https://images.unsplash.com/photo-1519608487953-e999c86e7455?w=300&auto=format&fit=crop"),
        Pair("Neon Light Tunnel", "https://images.unsplash.com/photo-1518770660439-4636190af475?w=300&auto=format&fit=crop"),
        Pair("Satisfying Flares", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=300&auto=format&fit=crop")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCosmos)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp) // Offset for bottom nav
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Header title with neon lighting background
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SSP CREATOR STUDIO",
                    color = NeonBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.drawWithNeonGlow(NeonBlue)
                )
                Text(
                    text = "Broadcast your custom cyber-loops onto the global feed",
                    color = CyberGray,
                    fontSize = 12.sp
                )
            }

            Divider(thickness = 1.dp, color = DarkStroke)

            if (isUploading) {
                // Compile loading panel screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, DarkStroke), RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { uploadProgress ?: 0.0f },
                            modifier = Modifier.size(80.dp),
                            color = NeonBlue,
                            strokeWidth = 6.dp,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )

                        Text(
                            text = "${((uploadProgress ?: 0f) * 100).toInt()}% READY",
                            color = NeonBlue,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )

                        // Compiling detailed logger status text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Log info",
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = compileStatusString,
                                    color = OffWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { uploadProgress ?: 0.0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = NeonBlue,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }
            } else {
                // Interactive Compose composite layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Step 1: Select Video Feed source
                    Text(
                        text = "1. SELECT BROADCAST LOOP",
                        color = OffWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        mockThumbnails.forEachIndexed { idx, pair ->
                            val isSelected = selectedVideoIndex == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.75f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) NeonBlue else Color.White.copy(alpha = 0.12f)
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedVideoIndex = idx },
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                AsyncImage(
                                    model = pair.second,
                                    contentDescription = pair.first,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Bottom overlay label
                                Text(
                                    text = "LOOP-${idx + 1}",
                                    color = if (isSelected) NeonBlue else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.8f))
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    style = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Step 2: Caption text inputs
                    Text(
                        text = "2. COMPILE DECK TEXT (CAPTION)",
                        color = OffWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        placeholder = {
                            Text(
                                "Enter cyber codes, description, hashtags (eg. #neon #2077)...",
                                color = CyberGray,
                                fontSize = 13.sp
                            )
                        },
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
                            .height(110.dp)
                            .testTag("caption_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Simulated Audio synthesizer panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ElectricIndigo.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Synth indicator",
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SYNTHESIZER DECK ACTIVE",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Auto-assigns SSPSynth Grid sound module.",
                                    color = CyberGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step 3: Trigger compilation & deploy
                    Button(
                        onClick = {
                            viewModel.simulateVideoUpload(captionText, selectedVideoIndex)
                            // We wait to trigger callback, let's allow flow logic to handle
                            scope.launch {
                                delay(2700)
                                captionText = ""
                                onUploadSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, NeonBlue), RoundedCornerShape(12.dp))
                            .testTag("compile_broadcast_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(ElectricIndigo, NeonBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardDoubleArrowUp,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "COMPILE & BROADCAST SHORT",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
