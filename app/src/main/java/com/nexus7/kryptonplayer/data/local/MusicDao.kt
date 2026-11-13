package com.nexus7.kryptonplayer.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun observeTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM songs")
    suspend fun getAllTracks(): List<TrackEntity>

    @Query("SELECT mediaId FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<Long>

    @Upsert
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query("SELECT mediaId FROM songs")
    suspend fun getAllTrackIds(): List<Long>

    @Query("DELETE FROM songs WHERE mediaId IN (:ids)")
    suspend fun deleteTracksByIds(ids: List<Long>)

    @Query("DELETE FROM songs")
    suspend fun deleteAllTracks()

    @Query("UPDATE songs SET isFavorite = :favorite WHERE mediaId = :mediaId")
    suspend fun setFavorite(mediaId: Long, favorite: Boolean)

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Upsert
    suspend fun upsertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Upsert
    suspend fun upsertPlaylistTracks(tracks: List<PlaylistTrackEntity>)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observePlaylistTracks(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND mediaId = :mediaId")
    suspend fun removeFromPlaylist(playlistId: Long, mediaId: Long)
}
