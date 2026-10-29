package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Artwork
import com.example.data.wallpaper.WallpaperTarget
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.viewmodel.WallpaperActionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WallpaperPreviewModal(
    artwork: Artwork,
    wallpaperState: WallpaperActionState,
    onDismiss: () -> Unit,
    onApply: (WallpaperTarget) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(WallpaperTarget.BOTH) }
    var previewMode by remember { mutableStateOf(false) } // false = Lock screen preview, true = Home screen preview

    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    val wallpaperPreviewText = stringResource(R.string.wallpaper_preview)
    val closePreviewText = stringResource(R.string.close_preview)
    val lockScreenText = stringResource(R.string.lock_screen)
    val homeScreenText = stringResource(R.string.home_screen)
    val bothScreensText = stringResource(R.string.both_screens)
    val settingWallpaperText = stringResource(R.string.setting_wallpaper)
    val setAsWallpaperText = stringResource(R.string.set_as_wallpaper_btn)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(GalleryBlack),
            color = GalleryBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = wallpaperPreviewText,
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldAccent
                        )
                        Text(
                            text = artwork.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_wallpaper_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = closePreviewText,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Phone Mockup Frame
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(9f / 18.5f)
                        .clip(RoundedCornerShape(32.dp))
                        .border(3.dp, Color(0xFF3E3935), RoundedCornerShape(32.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Artwork Background
                    AsyncImage(
                        model = artwork.bestDisplayImage,
                        contentDescription = artwork.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle Vignette Overlay for readable mock lock screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.55f)
                                    )
                                )
                            )
                    )

                    if (!previewMode) {
                        // Mock Lock Screen UI
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 44.dp, bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currentTime,
                                    fontSize = 58.sp,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Light,
                                    color = Color.White,
                                    letterSpacing = (-1).sp
                                )
                                Text(
                                    text = currentDate,
                                    fontSize = 13.sp,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            // Curator Watermark on Lock screen
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = artwork.title,
                                    fontFamily = PlayfairFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = artwork.artistDisplayName,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    color = GoldAccent,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Mock Home Screen Icons
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 50.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Bottom Dock
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.25f))
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.8f))
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Preview Screen type (Lock / Home)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!previewMode) GoldAccent else Color.Transparent)
                            .clickable { previewMode = false }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = lockScreenText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!previewMode) GalleryBlack else Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (previewMode) GoldAccent else Color.Transparent)
                            .clickable { previewMode = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = homeScreenText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (previewMode) GalleryBlack else Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Selector Buttons (Lock, Home, Both)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TargetChip(
                        label = lockScreenText,
                        isSelected = selectedTarget == WallpaperTarget.LOCK_SCREEN,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTarget = WallpaperTarget.LOCK_SCREEN }
                    )
                    TargetChip(
                        label = homeScreenText,
                        isSelected = selectedTarget == WallpaperTarget.HOME_SCREEN,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTarget = WallpaperTarget.HOME_SCREEN }
                    )
                    TargetChip(
                        label = bothScreensText,
                        isSelected = selectedTarget == WallpaperTarget.BOTH,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTarget = WallpaperTarget.BOTH }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Button
                Button(
                    onClick = { onApply(selectedTarget) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_set_wallpaper_button"),
                    enabled = wallpaperState !is WallpaperActionState.InProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = GalleryBlack
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (wallpaperState is WallpaperActionState.InProgress) {
                        CircularProgressIndicator(
                            color = GalleryBlack,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(settingWallpaperText, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = setAsWallpaperText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SurfaceContainerDark else SurfaceDark)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.8f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
