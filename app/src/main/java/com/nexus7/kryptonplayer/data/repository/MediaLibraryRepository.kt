package com.nexus7.kryptonplayer.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.room.withTransaction
import com.nexus7.kryptonplayer.data.local.MusicDao
import com.nexus7.kryptonplayer.data.local.MusicDatabase
import com.nexus7.kryptonplayer.data.media.MediaStoreScanner
import com.nexus7.kryptonplayer.domain.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface ScanOutcome {
    data class Success(val count: Int) : ScanOutcome
    data object PermissionRequired : ScanOutcome
    data class Failed(val reason: String) : ScanOutcome
}

@Singleton
class MediaLibraryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanner: MediaStoreScanner,
    private val database: MusicDatabase,
    private val dao: MusicDao,
) {
    private val observerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var contentObserver: ContentObserver? = null
    private val scanMutex = Mutex()

    fun observeTracks(): Flow<List<Track>> = dao.observeTracks().map { rows -> rows.map { it.toDomain() } }

    suspend fun setFavorite(trackId: Long, favorite: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(trackId, favorite)
    }

    suspend fun scanNow(): ScanOutcome = withContext(Dispatchers.IO) {
        scanMutex.withLock {
            if (!hasAudioPermission()) return@withLock ScanOutcome.PermissionRequired
            try {
                val oldFavorites = dao.getFavoriteIds().toHashSet()
                val scanned = scanner.readTracks().map { entity ->
                    entity.copy(isFavorite = entity.mediaId in oldFavorites)
                }
                database.withTransaction {
                    if (scanned.isEmpty()) {
                        dao.deleteAllTracks()
                    } else {
                        dao.upsertTracks(scanned)
                        dao.deleteTracksNotIn(scanned.map { it.mediaId })
                    }
                }
                registerChangeObserver()
                ScanOutcome.Success(scanned.size)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: SecurityException) {
                ScanOutcome.PermissionRequired
            } catch (error: Exception) {
                ScanOutcome.Failed(error.message ?: "The music library could not be scanned")
            }
        }
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    @Synchronized
    private fun registerChangeObserver() {
        if (contentObserver != null) return
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                observerScope.launch { scanNow() }
            }
        }
        runCatching {
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer,
            )
            contentObserver = observer
        }
    }
}
