package com.nexus7.kryptonplayer.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.nexus7.kryptonplayer.domain.model.Track
import com.nexus7.kryptonplayer.service.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var pendingPlayback: Pair<List<Track>, Int>? = null

    private val _state = MutableStateFlow(PlaybackUiState())
    val state = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish(player)
        }
    }

    init {
        connect()
        viewModelScope.launch {
            while (isActive) {
                controller?.let(::publish)
                delay(450)
            }
        }
    }

    private fun connect() {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        mediaController.addListener(playerListener)
                        publish(mediaController)
                        pendingPlayback?.let { (tracks, index) ->
                            pendingPlayback = null
                            startPlayback(mediaController, tracks, index)
                        }
                    }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun playTracks(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val activeController = controller
        if (activeController == null) {
            pendingPlayback = tracks.toList() to startIndex
            return
        }
        startPlayback(activeController, tracks, startIndex)
    }

    private fun startPlayback(activeController: MediaController, tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val items = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(track.artworkUri?.let(android.net.Uri::parse))
                        .build(),
                )
                .build()
        }
        activeController.setMediaItems(items, startIndex.coerceIn(items.indices), 0L)
        activeController.prepare()
        activeController.play()
    }

    fun togglePlayback() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun skipNext() { controller?.seekToNextMediaItem() }
    fun skipPrevious() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs.coerceAtLeast(0L)) }
    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }

    fun cycleRepeatMode() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    private fun publish(player: Player) {
        val metadata = player.currentMediaItem?.mediaMetadata
        val duration = player.duration.takeIf { it != androidx.media3.common.C.TIME_UNSET } ?: 0L
        _state.value = PlaybackUiState(
            mediaId = player.currentMediaItem?.mediaId,
            title = metadata?.title?.toString().orEmpty(),
            artist = metadata?.artist?.toString().orEmpty(),
            album = metadata?.albumTitle?.toString().orEmpty(),
            artworkUri = metadata?.artworkUri?.toString(),
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration.coerceAtLeast(0L),
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            hasCurrentItem = player.currentMediaItem != null,
        )
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
        super.onCleared()
    }
}

data class PlaybackUiState(
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val hasCurrentItem: Boolean = false,
)
