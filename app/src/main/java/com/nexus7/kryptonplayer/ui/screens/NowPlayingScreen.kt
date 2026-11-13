package com.nexus7.kryptonplayer.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nexus7.kryptonplayer.ui.components.GlassSurface
import com.nexus7.kryptonplayer.ui.viewmodel.PlaybackUiState
import dev.chrisbanes.haze.HazeState

@Composable
fun NowPlayingScreen(
    hazeState: HazeState,
    playback: PlaybackUiState,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
) {
    val progress = if (playback.durationMs > 0L) {
        playback.positionMs.toFloat() / playback.durationMs.toFloat()
    } else 0f
    var dragProgress by remember(playback.mediaId) { mutableFloatStateOf(-1f) }
    val displayedProgress by animateFloatAsState(
        targetValue = if (dragProgress >= 0f) dragProgress else progress.coerceIn(0f, 1f),
        label = "playback-progress",
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF08070C))) {
        if (playback.artworkUri != null) {
            AsyncImage(
                model = playback.artworkUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(70.dp),
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xC9080710), Color(0xE8090810), Color(0xFF08080C)),
                ),
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(playback.album.ifBlank { "Krypton Player" }, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
                IconButton(onClick = {}) { Icon(Icons.Rounded.MoreHoriz, contentDescription = "More") }
            }
            Spacer(Modifier.weight(0.8f))
            AsyncImage(
                model = playback.artworkUri,
                contentDescription = "Album artwork",
                modifier = Modifier
                    .fillMaxWidth(0.84f)
                    .weight(5f, fill = false)
                    .clip(RoundedCornerShape(28.dp)),
            )
            Spacer(Modifier.height(34.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(playback.title.ifBlank { "Nothing playing" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(playback.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(Modifier.height(18.dp))
            Slider(
                value = displayedProgress,
                onValueChange = { dragProgress = it },
                onValueChangeFinished = {
                    if (playback.durationMs > 0L) onSeek((dragProgress * playback.durationMs).toLong())
                    dragProgress = -1f
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime((displayedProgress * playback.durationMs).toLong()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTime(playback.durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onShuffle) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle", tint = if (playback.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(35.dp)) }
                IconButton(onClick = onToggle, modifier = Modifier.size(76.dp)) {
                    Icon(
                        if (playback.isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                        contentDescription = if (playback.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(35.dp)) }
                IconButton(onClick = onRepeat) {
                    Icon(Icons.Rounded.Repeat, contentDescription = "Repeat", tint = if (playback.repeatMode != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(20.dp))
            GlassSurface(hazeState, modifier = Modifier.fillMaxWidth(), cornerRadius = 22.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {}) { Icon(Icons.Rounded.FavoriteBorder, contentDescription = "Favorite") }
                    IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = "Queue") }
                    IconButton(onClick = {}) { Icon(Icons.Rounded.MoreHoriz, contentDescription = "More actions") }
                }
            }
            Spacer(Modifier.weight(0.4f))
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1_000L).coerceAtLeast(0L)
    return "%d:%02d".format(seconds / 60L, seconds % 60L)
}
