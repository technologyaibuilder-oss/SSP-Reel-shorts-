package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Forced Dark Cyber Theme for ultimate Futuristic / Gaming Aesthetic
private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    secondary = ElectricIndigo,
    tertiary = HotPink,
    background = DeepCosmos,
    surface = CardSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OffWhite,
    onSurface = OffWhite,
    surfaceVariant = Color(0xFF1E1E28),
    onSurfaceVariant = CyberGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode for Neon Cyber design
    dynamicColor: Boolean = false, // Overrule dynamic colors to preserve neon aesthetic integrity
    content: @Composable () -> Unit,
) {
    // We intentionally force CyberDarkColorScheme to maintain high-end Neon aesthetics
    val colorScheme = CyberDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

