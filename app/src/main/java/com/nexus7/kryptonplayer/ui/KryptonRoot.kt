package com.nexus7.kryptonplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.nexus7.kryptonplayer.data.repository.ScanOutcome
import com.nexus7.kryptonplayer.domain.model.Track
import com.nexus7.kryptonplayer.ui.components.GlassSurface
import com.nexus7.kryptonplayer.ui.screens.LibraryScreen
import com.nexus7.kryptonplayer.ui.screens.NowPlayingScreen
import com.nexus7.kryptonplayer.ui.screens.SettingsScreen
import com.nexus7.kryptonplayer.ui.screens.PlaylistsScreen
import com.nexus7.kryptonplayer.ui.viewmodel.LibraryViewModel
import com.nexus7.kryptonplayer.ui.viewmodel.PlaybackViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private enum class MainTab(val label: String) {
    Library("Library"), Search("Search"), Playlists("Playlists"), Settings("Settings"),
}

@Composable
fun KryptonRoot(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playbackViewModel: PlaybackViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val tracks by libraryViewModel.tracks.collectAsStateWithLifecycle()
    val playback by playbackViewModel.state.collectAsStateWithLifecycle()
    val scanOutcome by libraryViewModel.scanOutcome.collectAsStateWithLifecycle()
    val isScanning by libraryViewModel.isScanning.collectAsStateWithLifecycle()
    var activeTab by rememberSaveable { mutableStateOf(MainTab.Library.name) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (granted) libraryViewModel.refreshLibrary()
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) libraryViewModel.refreshLibrary()
    }

    val hazeState = rememberHazeState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF151020),
                    0.42f to Color(0xFF0D0C13),
                    1f to Color(0xFF08080C),
                ),
            )
            .hazeSource(hazeState),
    ) {
        if (!permissionGranted) {
            PermissionLanding(
                onGrant = {
                    permissionLauncher.launch(
                        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                        else Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                },
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                when (MainTab.valueOf(activeTab)) {
                    MainTab.Library -> LibraryScreen(
                        tracks = tracks,
                        isScanning = isScanning,
                        onRefresh = libraryViewModel::refreshLibrary,
                        onPlay = { item, index ->
                            playbackViewModel.playTracks(item, index)
                            showNowPlaying = true
                        },
                        onFavorite = libraryViewModel::setFavorite,
                    )
                    MainTab.Search -> LibraryScreen(
                        tracks = tracks,
                        isScanning = isScanning,
                        onRefresh = libraryViewModel::refreshLibrary,
                        onPlay = { item, index ->
                            playbackViewModel.playTracks(item, index)
                            showNowPlaying = true
                        },
                        onFavorite = libraryViewModel::setFavorite,
                        startInSearch = true,
                    )
                    MainTab.Playlists -> PlaylistsScreen()
                    MainTab.Settings -> SettingsScreen()
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                AnimatedVisibility(
                    visible = playback.hasCurrentItem && !showNowPlaying,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                ) {
                    MiniPlayer(
                        hazeState = hazeState,
                        title = playback.title,
                        artist = playback.artist,
                        artworkUri = playback.artworkUri,
                        isPlaying = playback.isPlaying,
                        onOpen = { showNowPlaying = true },
                        onToggle = playbackViewModel::togglePlayback,
                        onNext = playbackViewModel::skipNext,
                    )
                }
                MainNavigationBar(
                    hazeState = hazeState,
                    activeTab = activeTab,
                    onSelect = { activeTab = it.name },
                )
            }

            AnimatedVisibility(
                visible = showNowPlaying,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(spring()) + slideInVertically(initialOffsetY = { it / 8 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 8 }),
            ) {
                NowPlayingScreen(
                    hazeState = hazeState,
                    playback = playback,
                    onBack = { showNowPlaying = false },
                    onToggle = playbackViewModel::togglePlayback,
                    onNext = playbackViewModel::skipNext,
                    onPrevious = playbackViewModel::skipPrevious,
                    onSeek = playbackViewModel::seekTo,
                    onShuffle = playbackViewModel::toggleShuffle,
                    onRepeat = playbackViewModel::cycleRepeatMode,
                )
            }
        }

        if (scanOutcome is ScanOutcome.Failed) {
            // Failure details are kept in state; the next manual scan remains available.
        }
    }
}

@Composable
private fun PermissionLanding(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.LibraryMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(62.dp))
        Spacer(Modifier.height(22.dp))
        Text("Your music, beautifully organized", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "Krypton Player needs permission to read audio files on this device. Your music stays on your device.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(22.dp))
        Button(onClick = onGrant) { Text("Allow music access") }
    }
}

@Composable
private fun MiniPlayer(
    hazeState: dev.chrisbanes.haze.HazeState,
    title: String,
    artist: String,
    artworkUri: String?,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
) {
    GlassSurface(hazeState, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen), cornerRadius = 22.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = artworkUri,
                contentDescription = "Album artwork",
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(11.dp)),
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = 11.dp).clip(RoundedCornerShape(8.dp)),
            ) {
                Text(title.ifBlank { "Nothing playing" }, maxLines = 1, fontWeight = FontWeight.SemiBold)
                Text(artist, maxLines = 1, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onToggle) {
                Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play")
            }
            IconButton(onClick = onNext) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next") }
        }
    }
}

@Composable
private fun MainNavigationBar(
    hazeState: dev.chrisbanes.haze.HazeState,
    activeTab: String,
    onSelect: (MainTab) -> Unit,
) {
    GlassSurface(hazeState, modifier = Modifier.fillMaxWidth(), cornerRadius = 28.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MainTab.entries.forEach { tab ->
                val selected = activeTab == tab.name
                val icon = when (tab) {
                    MainTab.Library -> Icons.Rounded.LibraryMusic
                    MainTab.Search -> Icons.Rounded.Search
                    MainTab.Playlists -> Icons.Rounded.QueueMusic
                    MainTab.Settings -> Icons.Rounded.Settings
                }
                androidx.compose.material3.NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(tab) },
                    icon = { Icon(icon, contentDescription = tab.label) },
                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                    alwaysShowLabel = false,
                )
            }
        }
    }
}
