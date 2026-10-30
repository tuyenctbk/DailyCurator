package com.example.ui.components.tv

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Artwork
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.PlayfairFontFamily
import kotlinx.coroutines.delay

@Composable
fun AmbientArtFrameScreen(
    artworks: List<Artwork>,
    initialArtworkIndex: Int = 0,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onExit() }

    if (artworks.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().background(GalleryBlack))
        return
    }

    var currentIndex by remember { mutableIntStateOf(initialArtworkIndex.coerceIn(0, artworks.size - 1)) }
    var isPlaying by remember { mutableStateOf(true) }
    var showPlaque by remember { mutableStateOf(true) }

    val currentArtwork = artworks[currentIndex]

    // Slideshow interval timer
    LaunchedEffect(isPlaying, currentIndex, artworks.size) {
        if (isPlaying && artworks.isNotEmpty()) {
            delay(20_000L) // Rotate every 20 seconds
            currentIndex = (currentIndex + 1) % artworks.size
        }
    }

    // Auto-hide overlay plaque after 6 seconds of inactivity
    LaunchedEffect(currentIndex, showPlaque) {
        if (showPlaque) {
            delay(6_000L)
            showPlaque = false
        }
    }

    val prevPainting: () -> Unit = {
        currentIndex = if (currentIndex - 1 < 0) artworks.size - 1 else currentIndex - 1
        showPlaque = true
    }

    val nextPainting: () -> Unit = {
        currentIndex = (currentIndex + 1) % artworks.size
        showPlaque = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalleryBlack)
            .testTag("ambient_art_frame_screen")
            .focusable()
            .clickable {
                showPlaque = !showPlaque
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            prevPainting()
                            true
                        }
                        Key.DirectionRight -> {
                            nextPainting()
                            true
                        }
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                            isPlaying = !isPlaying
                            showPlaque = true
                            true
                        }
                        Key.Back, Key.Escape -> {
                            onExit()
                            true
                        }
                        Key.DirectionUp, Key.DirectionDown -> {
                            showPlaque = !showPlaque
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // Fullscreen Canvas Artwork with smooth Crossfade
        Crossfade(
            targetState = currentArtwork,
            animationSpec = tween(durationMillis = 1500),
            label = "AmbientArtworkCrossfade"
        ) { artwork ->
            AsyncImage(
                model = artwork.bestDisplayImage,
                contentDescription = artwork.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Top Subtle Action Bar
        AnimatedVisibility(
            visible = showPlaque,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier
                            .tvFocusable(shape = CircleShape)
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.exit_ambient_mode),
                            tint = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = stringResource(R.string.tv_mode_active),
                        fontFamily = PlayfairFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }

                // Controls: Previous, Play/Pause, Next
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = prevPainting,
                        modifier = Modifier
                            .tvFocusable(shape = CircleShape)
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            tint = ParchmentWhite
                        )
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .tvFocusable(shape = CircleShape)
                            .size(46.dp)
                            .background(GoldAccent, shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(if (isPlaying) R.string.pause_slideshow else R.string.play_slideshow),
                            tint = GalleryBlack
                        )
                    }

                    IconButton(
                        onClick = nextPainting,
                        modifier = Modifier
                            .tvFocusable(shape = CircleShape)
                            .size(42.dp)
                            .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ParchmentWhite
                        )
                    }
                }
            }
        }

        // Museum Gallery Plaque at Bottom
        AnimatedVisibility(
            visible = showPlaque,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentArtwork.title,
                            fontFamily = PlayfairFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = ParchmentWhite
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentArtwork.artistDisplayName,
                                fontFamily = OutfitFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = GoldAccent
                            )

                            if (currentArtwork.objectDate.isNotBlank()) {
                                Text(
                                    text = "•  ${currentArtwork.objectDate}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    color = ParchmentMuted
                                )
                            }

                            Text(
                                text = "•  ${currentArtwork.sourceMuseum}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                color = ParchmentMuted
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.tv_remote_hint),
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
