package com.nexus7.kryptonplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
