package com.nexus7.kryptonplayer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus7.kryptonplayer.data.repository.MediaLibraryRepository
import com.nexus7.kryptonplayer.data.repository.ScanOutcome
import com.nexus7.kryptonplayer.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MediaLibraryRepository,
) : ViewModel() {
    val tracks: StateFlow<List<Track>> = repository.observeTracks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _scanOutcome = MutableStateFlow<ScanOutcome?>(null)
    val scanOutcome = _scanOutcome.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    fun refreshLibrary() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            try {
                _scanOutcome.value = repository.scanNow()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun setFavorite(track: Track, favorite: Boolean) {
        viewModelScope.launch { repository.setFavorite(track.id, favorite) }
    }
}
