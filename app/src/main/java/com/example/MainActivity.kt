package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.DailyCuratorDatabase
import com.example.data.model.Artwork
import com.example.data.repository.CuratorRepository
import com.example.ui.components.CuratorBottomBar
import com.example.ui.components.SplashScreen
import com.example.ui.components.WallpaperPreviewModal
import com.example.ui.components.donation.DonationModalDialog
import com.example.ui.screens.ArtworkDetailScreen
import com.example.ui.screens.DailyDiscoveryScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.GalleryBrowseScreen
import com.example.ui.theme.DailyCuratorTheme
import com.example.ui.theme.GalleryBlack
import com.example.ui.viewmodel.CuratorScreen
import com.example.ui.viewmodel.CuratorViewModel
import com.example.ui.viewmodel.CuratorViewModelFactory
import com.example.ui.viewmodel.DownloadActionState
import com.example.ui.viewmodel.WallpaperActionState
import com.example.work.DailyWorkScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyCuratorTheme {
                DailyCuratorApp()
            }
        }
    }
}

@Composable
fun DailyCuratorApp() {
    val context = LocalContext.current
    val database = remember { DailyCuratorDatabase.getInstance(context) }
    val repository = remember { CuratorRepository(database.artworkDao(), database.artworkCacheDao()) }
    val viewModel: CuratorViewModel = viewModel(factory = CuratorViewModelFactory(repository))

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val dailyArtwork by viewModel.dailyArtwork.collectAsStateWithLifecycle()
    val galleryArtworks by viewModel.galleryArtworks.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val selectedArtwork by viewModel.selectedArtwork.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMovement by viewModel.selectedMovement.collectAsStateWithLifecycle()
    val selectedEra by viewModel.selectedEra.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val wallpaperState by viewModel.wallpaperState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val showDonationModal by viewModel.showDonationModal.collectAsStateWithLifecycle()
    val shouldShowBanner by viewModel.shouldShowBanner.collectAsStateWithLifecycle()
    val isSupporter by viewModel.isSupporter.collectAsStateWithLifecycle()

    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }
    val snackbarHostState = remember { SnackbarHostState() }

    var artworkForWallpaperModal by remember { mutableStateOf<Artwork?>(null) }
    var previousScreenBeforeDetail by remember { mutableStateOf(CuratorScreen.DAILY) }
    var pendingDownloadArtwork by remember { mutableStateOf<Artwork?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    // Initialize Donation Manager and WorkManager daily notifications
    LaunchedEffect(Unit) {
        viewModel.initDonationManager(context)
        DailyWorkScheduler.scheduleDailyNotifications(context)
    }

    // Notification permission launcher for Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(
                context,
                context.getString(R.string.notifications_enabled_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Storage permission launcher for legacy Android versions (< API 29)
    val writeStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingDownloadArtwork?.let { artwork ->
                viewModel.downloadMasterArtwork(context, artwork)
                pendingDownloadArtwork = null
            }
        } else {
            pendingDownloadArtwork = null
            Toast.makeText(
                context,
                context.getString(R.string.storage_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onTriggerDownload: (Artwork) -> Unit = { artwork ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownloadArtwork = artwork
            writeStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.downloadMasterArtwork(context, artwork)
        }
    }

    // Handle Wallpaper Feedback
    LaunchedEffect(wallpaperState) {
        when (val state = wallpaperState) {
            is WallpaperActionState.Success -> {
                artworkForWallpaperModal = null
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearActionStates()
            }
            is WallpaperActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearActionStates()
            }
            else -> Unit
        }
    }

    // Handle Download Feedback
    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadActionState.Success -> {
                val successMessage = context.getString(R.string.saved_success_format, state.title)
                snackbarHostState.showSnackbar(successMessage)
                viewModel.clearActionStates()
            }
            is DownloadActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearActionStates()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(GalleryBlack),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (currentScreen != CuratorScreen.DETAIL && !showSplash) {
                    CuratorBottomBar(
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            viewModel.navigateTo(screen)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == CuratorScreen.DETAIL) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                            )
                        } else if (initialState == CuratorScreen.DETAIL) {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        } else {
                            fadeIn().togetherWith(fadeOut())
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        CuratorScreen.DAILY -> {
                            DailyDiscoveryScreen(
                                artwork = dailyArtwork,
                                isFavorite = favoriteIds.contains(dailyArtwork.id),
                                downloadState = downloadState,
                                shouldShowBanner = shouldShowBanner,
                                isSupporter = isSupporter,
                                onInspect = {
                                    previousScreenBeforeDetail = CuratorScreen.DAILY
                                    viewModel.selectArtwork(dailyArtwork, openDetail = true)
                                },
                                onOpenWallpaperModal = {
                                    artworkForWallpaperModal = dailyArtwork
                                },
                                onDownload = {
                                    onTriggerDownload(dailyArtwork)
                                },
                                onToggleFavorite = {
                                    viewModel.toggleFavorite(dailyArtwork)
                                },
                                onShuffle = {
                                    viewModel.shuffleDailyPick()
                                },
                                onOpenDonationModal = {
                                    viewModel.openDonationModal()
                                },
                                onDismissBanner = {
                                    viewModel.dismissBanner()
                                }
                            )
                        }

                        CuratorScreen.GALLERY -> {
                            GalleryBrowseScreen(
                                artworks = galleryArtworks,
                                favoriteIds = favoriteIds,
                                searchQuery = searchQuery,
                                selectedMovement = selectedMovement,
                                selectedEra = selectedEra,
                                isSearching = isSearching,
                                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                                onMovementSelected = { viewModel.onMovementSelected(it) },
                                onEraSelected = { viewModel.onEraSelected(it) },
                                onSelectArtwork = { artwork ->
                                    previousScreenBeforeDetail = CuratorScreen.GALLERY
                                    viewModel.selectArtwork(artwork, openDetail = true)
                                },
                                onToggleFavorite = { artwork ->
                                    viewModel.toggleFavorite(artwork)
                                }
                            )
                        }

                        CuratorScreen.FAVORITES -> {
                            FavoritesScreen(
                                favorites = favorites,
                                isSupporter = isSupporter,
                                onSelectArtwork = { artwork ->
                                    previousScreenBeforeDetail = CuratorScreen.FAVORITES
                                    viewModel.selectArtwork(artwork, openDetail = true)
                                },
                                onToggleFavorite = { artwork ->
                                    viewModel.toggleFavorite(artwork)
                                },
                                onExploreArchive = {
                                    viewModel.navigateTo(CuratorScreen.GALLERY)
                                },
                                onOpenDonationModal = {
                                    viewModel.openDonationModal()
                                }
                            )
                        }

                        CuratorScreen.DETAIL -> {
                            selectedArtwork?.let { artwork ->
                                ArtworkDetailScreen(
                                    artwork = artwork,
                                    isFavorite = favoriteIds.contains(artwork.id),
                                    downloadState = downloadState,
                                    onBack = {
                                        viewModel.navigateTo(previousScreenBeforeDetail)
                                    },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(artwork)
                                    },
                                    onOpenWallpaperModal = {
                                        artworkForWallpaperModal = artwork
                                    },
                                    onDownload = {
                                        onTriggerDownload(artwork)
                                    }
                                )
                            }
                        }

                        CuratorScreen.WALLPAPER_PREVIEW -> Unit
                    }
                }

                // Interactive Wallpaper Preview Modal
                artworkForWallpaperModal?.let { artwork ->
                    WallpaperPreviewModal(
                        artwork = artwork,
                        wallpaperState = wallpaperState,
                        onDismiss = {
                            artworkForWallpaperModal = null
                        },
                        onApply = { target ->
                            viewModel.applyWallpaper(context, artwork, target)
                        }
                    )
                }
                // Donation Modal Dialog
                if (showDonationModal) {
                    DonationModalDialog(
                        isSupporter = isSupporter,
                        onDismiss = {
                            viewModel.closeDonationModal()
                        },
                        onSimulatePlayPurchase = {
                            viewModel.markAsSupporter()
                        }
                    )
                }
            }
        }

        // Animated Splash Overlay
        AnimatedVisibility(
            visible = showSplash,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SplashScreen(
                onTimeout = {
                    showSplash = false
                }
            )
        }
    }
}
