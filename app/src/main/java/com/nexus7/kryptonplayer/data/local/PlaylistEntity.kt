package com.nexus7.kryptonplayer.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "mediaId"],
    indices = [Index("mediaId"), Index(value = ["playlistId", "position"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["mediaId"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val mediaId: Long,
    val position: Int,
    val addedAt: Long,
)
