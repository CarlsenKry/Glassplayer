package com.nexus7.kryptonplayer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PlaylistsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Your playlists", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Playlist management is built on the Room schema and will be connected in implementation stage 4.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
