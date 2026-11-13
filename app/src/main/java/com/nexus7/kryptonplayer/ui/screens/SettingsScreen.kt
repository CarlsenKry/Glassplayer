package com.nexus7.kryptonplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp).padding(top = 28.dp)) {
        Text("SETTINGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text("Personalize", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        SettingsSection("Playback", "Fading, ReplayGain, equalizer and output profiles")
        SettingsSection("Appearance", "Liquid Glass, accent colors, light / dark / automatic themes")
        SettingsSection("Lock screen", "Artwork, media controls and optional full-screen player")
        SettingsSection("Library", "Scanning, folder exclusions and metadata preferences")
        SettingsSection("Lyrics", "Embedded lyrics, synchronized .lrc and lyrics display")
        SettingsSection("Backup", "Export and restore your playlists and settings")
        SettingsSection("About", "Krypton Player · Native Android · Version 0.1.0")
    }
}

@Composable
private fun SettingsSection(title: String, summary: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}
