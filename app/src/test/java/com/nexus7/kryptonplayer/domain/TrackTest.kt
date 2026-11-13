package com.nexus7.kryptonplayer.domain

import com.nexus7.kryptonplayer.domain.model.Track
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackTest {
    @Test
    fun durationLabelFormatsMinutesAndSeconds() {
        val track = Track(
            id = 1L,
            uri = "content://media/external/audio/media/1",
            title = "Test",
            artist = "Artist",
            album = "Album",
            albumId = 1L,
            durationMs = 185_000L,
            dateAddedSeconds = 0L,
            trackNumber = 1,
            year = 2026,
            artworkUri = null,
            relativePath = "Music/",
            genre = "Test",
            isFavorite = false,
        )
        assertEquals("3:05", track.durationLabel)
    }
}
