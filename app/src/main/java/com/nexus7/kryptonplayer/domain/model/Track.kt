package com.nexus7.kryptonplayer.domain.model

data class Track(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val dateAddedSeconds: Long,
    val trackNumber: Int,
    val year: Int,
    val artworkUri: String?,
    val relativePath: String,
    val genre: String,
    val isFavorite: Boolean,
) {
    val durationLabel: String
        get() {
            val totalSeconds = (durationMs / 1_000L).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
