package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.example.data.engagement.EngagementManager
import com.example.data.local.DailyCuratorDatabase
import com.example.data.model.Artwork
import com.example.data.repository.CuratorRepository
import com.example.ui.components.CuratorBottomBar
import com.example.ui.components.SplashScreen
import com.example.ui.components.WallpaperPreviewModal
import com.example.ui.components.donation.DonationModalDialog
import com.example.ui.components.tv.AmbientArtFrameScreen
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
import com.example.ui.viewmodel.EngagementUiEvent
import com.example.ui.viewmodel.WallpaperActionState
import com.example.work.DailyWorkScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.remote.NetworkClient.init(applicationContext)
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

    // Natural back navigation handling
    BackHandler(enabled = artworkForWallpaperModal != null) {
        artworkForWallpaperModal = null
    }
    BackHandler(enabled = showDonationModal) {
        viewModel.closeDonationModal()
    }
    BackHandler(enabled = currentScreen == CuratorScreen.DETAIL || currentScreen == CuratorScreen.AMBIENT_TV) {
        viewModel.navigateTo(previousScreenBeforeDetail)
    }
    BackHandler(enabled = currentScreen == CuratorScreen.GALLERY || currentScreen == CuratorScreen.FAVORITES) {
        viewModel.navigateTo(CuratorScreen.DAILY)
    }

    val engagementManager = remember { EngagementManager.getInstance(context) }

    // Initialize Engagement Manager and WorkManager daily notifications
    LaunchedEffect(Unit) {
        viewModel.initEngagementManager(context)
        DailyWorkScheduler.scheduleDailyNotifications(context)
        (context as? Activity)?.let { activity ->
            engagementManager.checkInAppUpdateSilently(activity)
        }
    }

    // Handle Moments of Delight: In-App Review requests
    LaunchedEffect(Unit) {
        viewModel.engagementEvents.collect { event ->
            when (event) {
                is EngagementUiEvent.RequestInAppReview -> {
                    (context as? Activity)?.let { activity ->
                        engagementManager.launchInAppReviewFlow(activity)
                    }
                }
            }
        }
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

    // Handle Download Feedback with Contextual Share Action (Zero Intrusive Popups)
    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            is DownloadActionState.Success -> {
                val successMessage = context.getString(R.string.saved_success_format, state.title)
                val actionLabel = context.getString(R.string.share_masterpiece)
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = successMessage,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, state.title)
                        putExtra(
                            Intent.EXTRA_TEXT,
                            context.getString(
                                R.string.share_text_format,
                                state.title,
                                context.getString(R.string.app_name),
                                "",
                                state.uri.toString()
                            )
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, actionLabel)
                    context.startActivity(shareIntent)
                }
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
                if (currentScreen != CuratorScreen.DETAIL && currentScreen != CuratorScreen.AMBIENT_TV && !showSplash && artworkForWallpaperModal == null) {
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
                        if (targetState == CuratorScreen.DETAIL || targetState == CuratorScreen.AMBIENT_TV) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                            )
                        } else if (initialState == CuratorScreen.DETAIL || initialState == CuratorScreen.AMBIENT_TV) {
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
                                },
                                onOpenAmbientMode = {
                                    previousScreenBeforeDetail = CuratorScreen.DAILY
                                    viewModel.navigateTo(CuratorScreen.AMBIENT_TV)
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
                                },
                                onOpenAmbientMode = {
                                    previousScreenBeforeDetail = CuratorScreen.GALLERY
                                    viewModel.navigateTo(CuratorScreen.AMBIENT_TV)
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
                                },
                                onOpenAmbientMode = {
                                    previousScreenBeforeDetail = CuratorScreen.FAVORITES
                                    viewModel.navigateTo(CuratorScreen.AMBIENT_TV)
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
                                    },
                                    onOpenAmbientMode = {
                                        previousScreenBeforeDetail = CuratorScreen.DETAIL
                                        viewModel.navigateTo(CuratorScreen.AMBIENT_TV)
                                    }
                                )
                            }
                        }

                        CuratorScreen.AMBIENT_TV -> {
                            val ambientArtworks = if (favorites.size >= 3) {
                                favorites
                            } else if (galleryArtworks.isNotEmpty()) {
                                galleryArtworks
                            } else {
                                listOf(dailyArtwork)
                            }
                            val targetId = selectedArtwork?.id ?: dailyArtwork.id
                            val initialIndex = ambientArtworks.indexOfFirst { it.id == targetId }.coerceAtLeast(0)
                            AmbientArtFrameScreen(
                                artworks = ambientArtworks,
                                initialArtworkIndex = initialIndex,
                                onExit = {
                                    viewModel.navigateTo(previousScreenBeforeDetail)
                                }
                            )
                        }

                        CuratorScreen.WALLPAPER_PREVIEW -> Unit
                    }
                }

                // Interactive Wallpaper Preview Modal
                AnimatedVisibility(
                    visible = artworkForWallpaperModal != null,
                    enter = fadeIn(tween(250)),
                    exit = fadeOut(tween(200))
                ) {
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
                }
                // Donation Modal Dialog
                if (showDonationModal) {
                    DonationModalDialog(
                        isSupporter = isSupporter,
                        onDismiss = {
                            viewModel.closeDonationModal()
                        },
                        onSupporterPurchase = {
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
