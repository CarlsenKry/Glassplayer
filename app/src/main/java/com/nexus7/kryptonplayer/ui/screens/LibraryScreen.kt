package com.nexus7.kryptonplayer.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.nexus7.kryptonplayer.domain.model.Track
import kotlinx.coroutines.launch

private val sections = listOf("Songs", "Albums", "Artists", "Genres", "Folders", "Playlists")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    tracks: List<Track>,
    isScanning: Boolean,
    onRefresh: () -> Unit,
    onPlay: (List<Track>, Int) -> Unit,
    onFavorite: (Track, Boolean) -> Unit,
    startInSearch: Boolean = false,
) {
    var section by rememberSaveable { mutableStateOf("Songs") }
    var query by rememberSaveable { mutableStateOf("") }
    var gridMode by rememberSaveable { mutableStateOf(false) }
    var sortMode by rememberSaveable { mutableStateOf("Title") }
    var showSortMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val filtered = remember(tracks, query, sortMode) {
        val searched = tracks.filter { track ->
            query.isBlank() || listOf(track.title, track.artist, track.album)
                .any { it.contains(query, ignoreCase = true) }
        }
        when (sortMode) {
            "Artist" -> searched.sortedBy { it.artist.lowercase() }
            "Date added" -> searched.sortedByDescending { it.dateAddedSeconds }
            "Duration" -> searched.sortedByDescending { it.durationMs }
            else -> searched.sortedBy { it.title.lowercase() }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("YOUR MUSIC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(
                    if (startInSearch) "Search" else "Library",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            IconButton(onClick = onRefresh, enabled = !isScanning) {
                if (isScanning) {
                    androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(21.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Rescan music")
                }
            }
            IconButton(onClick = { gridMode = !gridMode }) {
                Icon(if (gridMode) Icons.Rounded.List else Icons.Rounded.GridView, contentDescription = "Toggle grid or list")
            }
            Box {
                IconButton(onClick = { showSortMenu = true }) { Icon(Icons.Rounded.Sort, contentDescription = "Sort") }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    listOf("Title", "Artist", "Date added", "Duration").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { sortMode = option; showSortMenu = false },
                        )
                    }
                }
            }
        }

        if (startInSearch) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Songs, albums, artists") },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.07f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.045f),
                    focusedIndicatorColor = Color.White.copy(alpha = 0.24f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f),
                ),
            )
        }

        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sections) { label ->
                FilterChip(
                    selected = section == label,
                    onClick = { section = label },
                    label = { Text(label) },
                )
            }
        }

        when (section) {
            "Songs" -> {
                if (filtered.isEmpty()) EmptyLibraryState("No songs found", "Add audio files to your device, then refresh the library.")
                else if (gridMode) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 180.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(filtered, key = { it.id }) { track ->
                            TrackGridCard(track = track, onClick = { onPlay(filtered, filtered.indexOf(track)) })
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, end = 8.dp, top = 2.dp, bottom = 180.dp),
                        ) {
                            itemsIndexed(filtered, key = { _, track -> track.id }) { index, track ->
                                TrackRow(
                                    track = track,
                                    onClick = { onPlay(filtered, index) },
                                    onFavorite = { onFavorite(track, !track.isFavorite) },
                                )
                            }
                        }
                        AlphabetRail(
                            values = filtered.map { it.title },
                            onLetter = { letter ->
                                val index = filtered.indexOfFirst { it.title.startsWith(letter, ignoreCase = true) }
                                if (index >= 0) scope.launch { listState.animateScrollToItem(index) }
                            },
                        )
                    }
                }
            }
            "Albums" -> CollectionList(
                items = filtered.groupBy { it.album }.entries.sortedBy { it.key.lowercase() }.map { (name, group) ->
                    CollectionItem(name, group.firstOrNull()?.artist.orEmpty(), group.size, group.firstOrNull())
                }, onPlay = { item -> item.track?.let { onPlay(filtered, filtered.indexOf(it).coerceAtLeast(0)) } },
            )
            "Artists" -> CollectionList(
                items = filtered.groupBy { it.artist }.entries.sortedBy { it.key.lowercase() }.map { (name, group) ->
                    CollectionItem(name, "Artist", group.size, group.firstOrNull())
                }, onPlay = { item -> item.track?.let { onPlay(filtered, filtered.indexOf(it).coerceAtLeast(0)) } },
            )
            "Genres" -> CollectionList(
                items = filtered.groupBy { it.genre }.entries.sortedBy { it.key.lowercase() }.map { (name, group) ->
                    CollectionItem(name, "Genre", group.size, group.firstOrNull())
                }, onPlay = { item -> item.track?.let { onPlay(filtered, filtered.indexOf(it).coerceAtLeast(0)) } },
            )
            "Folders" -> CollectionList(
                items = filtered.groupBy { it.relativePath.ifBlank { "Device audio" } }
                    .entries.sortedBy { it.key.lowercase() }.map { (name, group) ->
                        CollectionItem(name.trimEnd('/'), "Folder", group.size, group.firstOrNull())
                    }, onPlay = { item -> item.track?.let { onPlay(filtered, filtered.indexOf(it).coerceAtLeast(0)) } },
            )
            else -> EmptyLibraryState("Playlists are next", "Playlist creation, editing, imports and smart playlists are part of the next implementation stage.")
        }
    }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit, onFavorite: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).clickable(onClick = onClick).padding(vertical = 7.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(track.artworkUri, Modifier.size(54.dp))
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(
                "${track.artist}  •  ${track.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(track.durationLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        IconButton(onClick = onFavorite) {
            Icon(
                if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (track.isFavorite) "Remove favorite" else "Add favorite",
                tint = if (track.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrackGridCard(track: Track, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)) {
        CoverArt(track.artworkUri, Modifier.fillMaxWidth().height(158.dp), radius = 18.dp)
        Spacer(Modifier.height(8.dp))
        Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CoverArt(uri: String?, modifier: Modifier, radius: androidx.compose.ui.unit.Dp = 12.dp) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(radius)).background(Color(0xFF282133)),
        contentAlignment = Alignment.Center,
    ) {
        if (uri != null) {
            AsyncImage(model = uri, contentDescription = "Album artwork", modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFFD7C4F0), modifier = Modifier.size(30.dp))
        }
    }
}

private data class CollectionItem(val title: String, val subtitle: String, val count: Int, val track: Track?)

@Composable
private fun CollectionList(items: List<CollectionItem>, onPlay: (CollectionItem) -> Unit) {
    if (items.isEmpty()) {
        EmptyLibraryState("Nothing here yet", "Your library will appear after a scan.")
    } else {
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, bottom = 180.dp)) {
            items(items, key = { it.title }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { onPlay(item) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CoverArt(item.track?.artworkUri, Modifier.size(58.dp))
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        Text("${item.subtitle}  •  ${item.count} tracks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlphabetRail(values: List<String>, onLetter: (String) -> Unit) {
    val letters = remember(values) { values.mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.distinct().sorted() }
    Column(
        modifier = Modifier.width(25.dp).padding(end = 5.dp, top = 2.dp, bottom = 180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        letters.forEach { letter ->
            Text(
                letter,
                modifier = Modifier.clickable { onLetter(letter) }.padding(vertical = 1.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptyLibraryState(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.Album, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
