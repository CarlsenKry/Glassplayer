package com.nexus7.kryptonplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KryptonColors = darkColorScheme(
    primary = Color(0xFFE4C9FF),
    onPrimary = Color(0xFF27123B),
    secondary = Color(0xFFBBA7F8),
    onSecondary = Color(0xFF20143B),
    background = Color(0xFF09090D),
    onBackground = Color(0xFFF5F0FA),
    surface = Color(0xFF15131B),
    onSurface = Color(0xFFF5F0FA),
    surfaceVariant = Color(0xFF282431),
    onSurfaceVariant = Color(0xFFC8C0D0),
    outline = Color(0xFF51485E),
)

@Composable
fun KryptonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KryptonColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
