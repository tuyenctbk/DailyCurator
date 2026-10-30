package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Artwork
import com.example.ui.theme.DividerDark
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.viewmodel.DownloadActionState
import com.example.ui.components.donation.DonationBannerCard
import com.example.ui.components.donation.SupporterBadge
import androidx.compose.material.icons.filled.VolunteerActivism
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Tv
import com.example.ui.components.tv.tvFocusable

@Composable
fun DailyDiscoveryScreen(
    artwork: Artwork,
    isFavorite: Boolean,
    downloadState: DownloadActionState,
    shouldShowBanner: Boolean,
    isSupporter: Boolean,
    onInspect: () -> Unit,
    onOpenWallpaperModal: () -> Unit,
    onDownload: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShuffle: () -> Unit,
    onOpenDonationModal: () -> Unit,
    onDismissBanner: () -> Unit,
    onOpenAmbientMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()).uppercase()
    }

    val dailyCuratorHeader = stringResource(R.string.daily_curator_header)
    val shuffleDesc = stringResource(R.string.shuffle_content_desc)
    val zoom4k = stringResource(R.string.zoom_4k)
    val cc0PublicDomain = stringResource(R.string.cc0_public_domain)
    val setWallpaperText = stringResource(R.string.set_wallpaper)
    val save4kText = stringResource(R.string.save_4k)
    val curatorsAnalysis = stringResource(R.string.curators_analysis)
    val mediumText = stringResource(R.string.medium_label)
    val dimensionsText = stringResource(R.string.dimensions_label)
    val creditText = stringResource(R.string.credit_label)
    val departmentText = stringResource(R.string.department_label)
    val sourceText = stringResource(R.string.source_label)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalleryBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("daily_discovery_screen")
    ) {
        // Daily Curator Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = dailyCuratorHeader,
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 1.5.sp
                    )
                }

                Text(
                    text = todayDateFormatted,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSupporter) {
                    SupporterBadge()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Ambient Art Frame / Living Room TV button
                IconButton(
                    onClick = onOpenAmbientMode,
                    modifier = Modifier
                        .tvFocusable(shape = CircleShape, onDpadCenterClick = onOpenAmbientMode)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = stringResource(R.string.ambient_art_frame),
                        tint = GoldAccent
                    )
                }

                IconButton(
                    onClick = onOpenDonationModal,
                    modifier = Modifier
                        .tvFocusable(shape = CircleShape, onDpadCenterClick = onOpenDonationModal)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = stringResource(R.string.donation_title),
                        tint = GoldAccent
                    )
                }

                IconButton(
                    onClick = onShuffle,
                    modifier = Modifier
                        .tvFocusable(shape = CircleShape, onDpadCenterClick = onShuffle)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .testTag("shuffle_daily_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = shuffleDesc,
                        tint = GoldAccent
                    )
                }
            }
        }

        if (shouldShowBanner && !isSupporter) {
            Spacer(modifier = Modifier.height(12.dp))
            DonationBannerCard(
                onOpenDonationModal = onOpenDonationModal,
                onDismiss = onDismissBanner
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero Masterpiece Framed Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tvFocusable(shape = RoundedCornerShape(20.dp), onDpadCenterClick = onInspect)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .clickable(onClick = onInspect)
                .testTag("daily_artwork_hero_canvas")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3.2f)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = artwork.bestDisplayImage,
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Atmospheric Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                // Tap to Zoom Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = zoom4k,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = zoom4k,
                            fontSize = 11.sp,
                            fontFamily = OutfitFontFamily,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Public Domain / CC0 Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cc0PublicDomain,
                        fontSize = 10.sp,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GalleryBlack,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title and Artist Info
        Text(
            text = artwork.title,
            style = MaterialTheme.typography.displayMedium,
            color = ParchmentWhite,
            modifier = Modifier.testTag("daily_artwork_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = artwork.artistDisplayName,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = PlayfairFontFamily,
            color = GoldAccent
        )

        if (artwork.artistDisplayBio.isNotBlank()) {
            Text(
                text = artwork.artistDisplayBio,
                style = MaterialTheme.typography.bodyMedium,
                color = ParchmentMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val museumName = if (artwork.sourceMuseum.contains("Chicago")) {
            stringResource(R.string.art_institute)
        } else {
            stringResource(R.string.met_museum)
        }

        // Movement & Museum Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoChip(label = artwork.movement)
            if (artwork.objectDate.isNotBlank()) {
                InfoChip(label = artwork.objectDate)
            }
            InfoChip(label = museumName)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onOpenWallpaperModal,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .tvFocusable(shape = RoundedCornerShape(12.dp), onDpadCenterClick = onOpenWallpaperModal)
                    .testTag("daily_set_wallpaper_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = GalleryBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Wallpaper,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = setWallpaperText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onDownload,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .tvFocusable(shape = RoundedCornerShape(12.dp), onDpadCenterClick = onDownload)
                    .testTag("daily_download_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ParchmentWhite
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                shape = RoundedCornerShape(12.dp),
                enabled = downloadState !is DownloadActionState.InProgress
            ) {
                if (downloadState is DownloadActionState.InProgress) {
                    CircularProgressIndicator(
                        color = GoldAccent,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = save4kText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(50.dp)
                    .tvFocusable(shape = RoundedCornerShape(12.dp), onDpadCenterClick = onToggleFavorite)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, DividerDark, RoundedCornerShape(12.dp))
                    .testTag("daily_favorite_button")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(if (isFavorite) R.string.remove_favorite_desc else R.string.add_favorite_desc),
                    tint = if (isFavorite) GoldAccent else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Curatorial Notes & Cultural History
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = curatorsAnalysis,
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                val defaultCuratorNote = stringResource(R.string.default_curator_note_daily)
                val defaultMedium = stringResource(R.string.default_medium)
                val defaultCanvasDimensions = stringResource(R.string.default_canvas_dimensions)
                val defaultMuseumGift = stringResource(R.string.default_museum_gift)
                val defaultArtWing = stringResource(R.string.default_art_wing)

                Text(
                    text = artwork.curatorNote.ifBlank { defaultCuratorNote },
                    style = MaterialTheme.typography.bodyLarge,
                    color = ParchmentWhite.copy(alpha = 0.9f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Provenance
                ProvenanceRow(mediumText, artwork.medium.ifBlank { defaultMedium })
                ProvenanceRow(dimensionsText, artwork.dimensions.ifBlank { defaultCanvasDimensions })
                ProvenanceRow(creditText, artwork.creditLine.ifBlank { defaultMuseumGift })
                ProvenanceRow(departmentText, artwork.department.ifBlank { defaultArtWing })
                ProvenanceRow(sourceText, artwork.sourceMuseum)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerDark)
            .border(1.dp, DividerDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = OutfitFontFamily,
            color = ParchmentWhite.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProvenanceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.65f)
        )
    }
}
