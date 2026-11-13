package com.nexus7.kryptonplayer.data.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.nexus7.kryptonplayer.data.local.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun readTracks(): List<TrackEntity> {
        val resolver = context.contentResolver
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.YEAR)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
        }.toTypedArray()

        val genreByAudioId = readGenres()
        val tracks = mutableListOf<TrackEntity>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        resolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val pathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                -1
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val title = cursor.getString(titleIndex)?.takeIf(String::isNotBlank) ?: "Unknown title"
                val artist = cursor.getString(artistIndex)?.takeIf(String::isNotBlank)
                    ?.takeUnless { it == MediaStore.UNKNOWN_STRING } ?: "Unknown artist"
                val album = cursor.getString(albumIndex)?.takeIf(String::isNotBlank)
                    ?.takeUnless { it == MediaStore.UNKNOWN_STRING } ?: "Unknown album"
                val songUri = ContentUris.withAppendedId(collection, id)
                val artUri = if (albumId > 0L) {
                    ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId,
                    ).toString()
                } else null

                tracks += TrackEntity(
                    mediaId = id,
                    uri = songUri.toString(),
                    title = title,
                    artist = artist,
                    album = album,
                    albumId = albumId,
                    durationMs = cursor.getLong(durationIndex).coerceAtLeast(0L),
                    dateAddedSeconds = cursor.getLong(dateAddedIndex),
                    trackNumber = cursor.getInt(trackIndex).coerceAtLeast(0),
                    year = cursor.getInt(yearIndex).coerceAtLeast(0),
                    artworkUri = artUri,
                    relativePath = if (pathIndex >= 0) cursor.getString(pathIndex).orEmpty() else "",
                    genre = genreByAudioId[id].orEmpty().ifBlank { "Unknown genre" },
                )
            }
        }
        return tracks
    }

    private fun readGenres(): Map<Long, String> {
        val result = mutableMapOf<Long, String>()
        val resolver = context.contentResolver
        val genresUri = MediaStore.Audio.Genres.getContentUri("external")
        resolver.query(
            genresUri,
            arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
            null,
            null,
            null,
        )?.use { genres ->
            val idIndex = genres.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
            val nameIndex = genres.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
            while (genres.moveToNext()) {
                val genreId = genres.getLong(idIndex)
                val name = genres.getString(nameIndex)?.takeIf(String::isNotBlank) ?: continue
                val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                resolver.query(
                    membersUri,
                    arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                    null,
                    null,
                    null,
                )?.use { members ->
                    val audioIdIndex = members.getColumnIndexOrThrow(MediaStore.Audio.Genres.Members.AUDIO_ID)
                    while (members.moveToNext()) result[members.getLong(audioIdIndex)] = name
                }
            }
        }
        return result
    }
}
