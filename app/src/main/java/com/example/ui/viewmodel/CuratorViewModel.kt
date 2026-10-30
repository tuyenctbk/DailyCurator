package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Artwork
import com.example.data.repository.CuratorRepository
import com.example.data.wallpaper.WallpaperHelper
import com.example.data.wallpaper.WallpaperTarget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.R

sealed class WallpaperActionState {
    object Idle : WallpaperActionState()
    object InProgress : WallpaperActionState()
    data class Success(val message: String) : WallpaperActionState()
    data class Error(val message: String) : WallpaperActionState()
}

sealed class DownloadActionState {
    object Idle : DownloadActionState()
    object InProgress : DownloadActionState()
    data class Success(val uri: Uri, val title: String) : DownloadActionState()
    data class Error(val message: String) : DownloadActionState()
}

enum class CuratorScreen {
    DAILY,
    GALLERY,
    FAVORITES,
    DETAIL,
    WALLPAPER_PREVIEW,
    AMBIENT_TV
}

class CuratorViewModel(
    private val repository: CuratorRepository
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(CuratorScreen.DAILY)
    val currentScreen: StateFlow<CuratorScreen> = _currentScreen.asStateFlow()

    // Daily artwork flow collected reactively from repository
    val dailyArtwork: StateFlow<Artwork> = repository.dailyArtworkFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.getDailyArtwork()
    )

    private val _selectedArtwork = MutableStateFlow<Artwork?>(repository.getDailyArtwork())
    val selectedArtwork: StateFlow<Artwork?> = _selectedArtwork.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMovement = MutableStateFlow("All")
    val selectedMovement: StateFlow<String> = _selectedMovement.asStateFlow()

    private val _selectedEra = MutableStateFlow("All")
    val selectedEra: StateFlow<String> = _selectedEra.asStateFlow()

    private val _galleryArtworks = MutableStateFlow<List<Artwork>>(repository.getAllCurated())
    val galleryArtworks: StateFlow<List<Artwork>> = _galleryArtworks.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _wallpaperState = MutableStateFlow<WallpaperActionState>(WallpaperActionState.Idle)
    val wallpaperState: StateFlow<WallpaperActionState> = _wallpaperState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadActionState>(DownloadActionState.Idle)
    val downloadState: StateFlow<DownloadActionState> = _downloadState.asStateFlow()

    val favorites: StateFlow<List<Artwork>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _showDonationModal = MutableStateFlow(false)
    val showDonationModal: StateFlow<Boolean> = _showDonationModal.asStateFlow()

    private val _shouldShowBanner = MutableStateFlow(false)
    val shouldShowBanner: StateFlow<Boolean> = _shouldShowBanner.asStateFlow()

    private val _isSupporter = MutableStateFlow(false)
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private var donationManager: com.example.data.donation.DonationManager? = null
    private var searchJob: Job? = null

    init {
        performSearch()
    }

    fun initDonationManager(context: Context) {
        val manager = com.example.data.donation.DonationManager.getInstance(context)
        donationManager = manager
        _isSupporter.value = manager.isSupporter.value
        _shouldShowBanner.value = manager.shouldShowBanner()

        if (manager.shouldShowAutoPrompt()) {
            _showDonationModal.value = true
            manager.recordAutoPromptShown()
        }
    }

    fun openDonationModal() {
        _showDonationModal.value = true
    }

    fun closeDonationModal() {
        _showDonationModal.value = false
    }

    fun dismissBanner() {
        donationManager?.dismissBanner()
        _shouldShowBanner.value = false
    }

    fun recordSuccessfulOperation() {
        donationManager?.recordSuccessfulOperation()
        donationManager?.let { manager ->
            _shouldShowBanner.value = manager.shouldShowBanner()
            if (manager.shouldShowAutoPrompt()) {
                _showDonationModal.value = true
                manager.recordAutoPromptShown()
            }
        }
    }

    fun markAsSupporter() {
        donationManager?.markAsSupporter()
        _isSupporter.value = true
        _shouldShowBanner.value = false
        _showDonationModal.value = false
    }

    fun navigateTo(screen: CuratorScreen) {
        _currentScreen.value = screen
    }

    fun selectArtwork(artwork: Artwork, openDetail: Boolean = true) {
        _selectedArtwork.value = artwork
        if (openDetail) {
            _currentScreen.value = CuratorScreen.DETAIL
        }
    }

    fun shuffleDailyPick() {
        viewModelScope.launch {
            val result = repository.refreshDailyArtwork()
            if (result.isSuccess) {
                val pick = result.getOrNull()
                if (pick != null) {
                    _selectedArtwork.value = pick
                }
            } else {
                // Fallback local shuffle
                val all = repository.getAllCurated()
                val currentId = dailyArtwork.value.id
                val otherArtworks = all.filter { it.id != currentId }
                if (otherArtworks.isNotEmpty()) {
                    val random = otherArtworks.random()
                    repository.setDailyArtwork(random)
                }
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        triggerDebouncedSearch()
    }

    fun onMovementSelected(movement: String) {
        _selectedMovement.value = movement
        performSearch()
    }

    fun onEraSelected(era: String) {
        _selectedEra.value = era
        performSearch()
    }

    fun toggleFavorite(artwork: Artwork) {
        viewModelScope.launch {
            repository.toggleFavorite(artwork)
        }
    }

    private fun triggerDebouncedSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            performSearch()
        }
    }

    fun performSearch() {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = repository.searchArtworks(
                    query = _searchQuery.value,
                    selectedMovement = _selectedMovement.value,
                    selectedEra = _selectedEra.value
                )
                _galleryArtworks.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun applyWallpaper(context: Context, artwork: Artwork, target: WallpaperTarget) {
        viewModelScope.launch {
            _wallpaperState.value = WallpaperActionState.InProgress
            val bitmap = WallpaperHelper.fetchBitmap(context, artwork.bestDisplayImage)
            if (bitmap == null) {
                _wallpaperState.value = WallpaperActionState.Error(context.getString(R.string.wallpaper_error_load))
                return@launch
            }

            val result = WallpaperHelper.setWallpaper(context, bitmap, target)
            if (result.isSuccess) {
                recordSuccessfulOperation()
                val targetName = when (target) {
                    WallpaperTarget.HOME_SCREEN -> context.getString(R.string.home_screen)
                    WallpaperTarget.LOCK_SCREEN -> context.getString(R.string.lock_screen)
                    WallpaperTarget.BOTH -> context.getString(R.string.both_screens)
                }
                _wallpaperState.value = WallpaperActionState.Success(
                    context.getString(R.string.wallpaper_success_format, targetName)
                )
            } else {
                _wallpaperState.value = WallpaperActionState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: context.getString(R.string.wallpaper_error_set)
                )
            }
        }
    }

    fun downloadMasterArtwork(context: Context, artwork: Artwork) {
        viewModelScope.launch {
            _downloadState.value = DownloadActionState.InProgress
            val bitmap = WallpaperHelper.fetchBitmap(context, artwork.bestDisplayImage)
            if (bitmap == null) {
                _downloadState.value = DownloadActionState.Error(context.getString(R.string.download_error_retrieve))
                return@launch
            }

            val result = WallpaperHelper.downloadImageToGallery(
                context = context,
                bitmap = bitmap,
                title = artwork.title,
                artist = artwork.artistDisplayName
            )

            if (result.isSuccess) {
                recordSuccessfulOperation()
                val uri = result.getOrNull()!!
                _downloadState.value = DownloadActionState.Success(uri, artwork.title)
            } else {
                _downloadState.value = DownloadActionState.Error(
                    result.exceptionOrNull()?.localizedMessage ?: context.getString(R.string.download_error_failed)
                )
            }
        }
    }

    fun clearActionStates() {
        _wallpaperState.value = WallpaperActionState.Idle
        _downloadState.value = DownloadActionState.Idle
    }
}

class CuratorViewModelFactory(
    private val repository: CuratorRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CuratorViewModel(repository) as T
    }
}
