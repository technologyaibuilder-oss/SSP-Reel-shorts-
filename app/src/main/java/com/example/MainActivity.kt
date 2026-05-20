package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.screens.CreatorStudioScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReelsFeedScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize modern AppViewModel via Factory inject
                val appViewModel: AppViewModel = viewModel(
                    factory = AppViewModelFactory(application)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepCosmos
                ) {
                    MainAppScaffold(viewModel = appViewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf("feed") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Active Screen dynamic frame
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                "feed" -> ReelsFeedScreen(viewModel = viewModel)
                "studio" -> CreatorStudioScreen(
                    viewModel = viewModel,
                    onUploadSuccess = {
                        selectedTab = "feed"
                    }
                )
                "alerts" -> NotificationsScreen(viewModel = viewModel)
                "profile" -> UserProfileScreen(viewModel = viewModel)
            }
        }

        // Breathtaking Floating Glassmorphic Bottom Navigation Deck
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            CyberBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

@Composable
fun CyberBottomBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Safe-zone gesture pill compatibility
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(24.dp))
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CyberTabButton(
                icon = Icons.Default.VideoLibrary,
                label = "Feed",
                isSelected = selectedTab == "feed",
                testTag = "nav_tab_feed",
                onClick = { onTabSelected("feed") }
            )
            CyberTabButton(
                icon = Icons.Default.AddBox,
                label = "Studio",
                isSelected = selectedTab == "studio",
                testTag = "nav_tab_studio",
                onClick = { onTabSelected("studio") }
            )
            CyberTabButton(
                icon = Icons.Default.Notifications,
                label = "Alerts",
                isSelected = selectedTab == "alerts",
                testTag = "nav_tab_alerts",
                onClick = { onTabSelected("alerts") }
            )
            CyberTabButton(
                icon = Icons.Default.AccountCircle,
                label = "Profile",
                isSelected = selectedTab == "profile",
                testTag = "nav_tab_profile",
                onClick = { onTabSelected("profile") }
            )
        }
    }
}

@Composable
fun RowScope.CyberTabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .testTag(testTag)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) NeonBlue else CyberGray,
                modifier = Modifier.size(24.dp)
            )

            // Dynamic bottom glowing dot under the selected tab
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .offset(y = 16.dp)
                        .size(4.dp)
                        .background(NeonBlue, CircleShape)
                        .border(BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f)), CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) NeonBlue else CyberGray,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}
