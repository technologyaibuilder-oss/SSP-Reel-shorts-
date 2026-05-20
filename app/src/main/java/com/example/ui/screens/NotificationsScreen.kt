package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationItem
import com.example.ui.AppViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.notifications.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCosmos)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Height offset for bottom navigation bar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header title
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ALERTS DECK",
                    color = NeonBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.drawWithNeonGlow(NeonBlue)
                )
                Text(
                    text = "Monitor feedback loops and system messages.",
                    color = CyberGray,
                    fontSize = 12.sp
                )
            }

            Divider(thickness = 1.dp, color = DarkStroke)

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Empty Alerts",
                            tint = CyberGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No system logs compiled.",
                            color = CyberGray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertItemCard(alert)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(alert: NotificationItem) {
    val timeFormatted = remember(alert.timestamp) {
        try {
            val sdf = SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault())
            sdf.format(Date(alert.timestamp))
        } catch (e: Exception) {
            "Just now"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(NeonBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFormatted,
                        color = ElectricIndigo,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.body,
                    color = OffWhite,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
