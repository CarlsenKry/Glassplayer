package com.nexus7.kryptonplayer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nexus7.kryptonplayer.domain.model.Track

@Entity(tableName = "songs")
data class TrackEntity(
    @PrimaryKey val mediaId: Long,
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
    val isFavorite: Boolean = false,
) {
    fun toDomain() = Track(
        id = mediaId,
        uri = uri,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        durationMs = durationMs,
        dateAddedSeconds = dateAddedSeconds,
        trackNumber = trackNumber,
        year = year,
        artworkUri = artworkUri,
        relativePath = relativePath,
        genre = genre,
        isFavorite = isFavorite,
    )
}
