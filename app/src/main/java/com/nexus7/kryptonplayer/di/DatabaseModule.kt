package com.nexus7.kryptonplayer.di

import android.content.Context
import androidx.room.Room
import com.nexus7.kryptonplayer.data.local.MusicDao
import com.nexus7.kryptonplayer.data.local.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, "krypton-library.db")
            .build()

    @Provides
    fun provideMusicDao(database: MusicDatabase): MusicDao = database.musicDao()
}
