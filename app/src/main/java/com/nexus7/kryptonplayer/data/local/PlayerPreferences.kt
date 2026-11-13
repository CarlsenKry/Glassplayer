package com.nexus7.kryptonplayer.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.playerDataStore by preferencesDataStore(name = "player_settings")

@Singleton
class PlayerPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val reduceTransparency = booleanPreferencesKey("reduce_transparency")
    }

    val theme: Flow<String> = context.playerDataStore.data.map { it[Keys.theme] ?: "system" }
    val reduceTransparency: Flow<Boolean> = context.playerDataStore.data.map {
        it[Keys.reduceTransparency] ?: false
    }

    suspend fun setTheme(value: String) {
        context.playerDataStore.edit { it[Keys.theme] = value }
    }

    suspend fun setReduceTransparency(value: Boolean) {
        context.playerDataStore.edit { it[Keys.reduceTransparency] = value }
    }
}
