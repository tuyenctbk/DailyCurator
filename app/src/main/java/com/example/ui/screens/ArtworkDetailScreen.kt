package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.SurfaceDark
import com.example.ui.viewmodel.DownloadActionState
import com.example.ui.components.tv.tvFocusable

@Composable
fun ArtworkDetailScreen(
    artwork: Artwork,
    isFavorite: Boolean,
    downloadState: DownloadActionState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenWallpaperModal: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Handle back gesture properly
    BackHandler {
        onBack()
    }

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4.5f)
        if (scale > 1f) {
            val maxOffset = (scale - 1f) * 400f
            offset = Offset(
                x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
                y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
            )
        } else {
            offset = Offset.Zero
        }
    }

    val shareMasterpieceTitle = stringResource(R.string.share_masterpiece)
    val shareText = stringResource(
        R.string.share_text_format,
        artwork.title,
        artwork.artistDisplayName,
        artwork.objectDate,
        artwork.bestDisplayImage
    )
    val historicalContextText = stringResource(R.string.historical_context)
    val artistBiographyText = stringResource(R.string.artist_biography)
    val pinchToInspectText = stringResource(R.string.pinch_to_inspect)
    val cc0PublicDomainText = stringResource(R.string.cc0_public_domain)
    val setWallpaperText = stringResource(R.string.set_wallpaper)
    val save4kText = stringResource(R.string.save_4k)
    val mediumText = stringResource(R.string.medium_label)
    val dimensionsText = stringResource(R.string.dimensions_label)
    val creditText = stringResource(R.string.credit_label)
    val departmentText = stringResource(R.string.department_label)
    val sourceText = stringResource(R.string.source_label)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GalleryBlack)
            .testTag("artwork_detail_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backDesc = stringResource(R.string.back_button_desc)
                val favoriteDesc = stringResource(if (isFavorite) R.string.remove_favorite_desc else R.string.add_favorite_desc)

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .tvFocusable(shape = CircleShape, onDpadCenterClick = onBack)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backDesc,
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_SUBJECT, "${artwork.title} — ${artwork.artistDisplayName}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, shareMasterpieceTitle)
                        context.startActivity(shareIntent)
                    }

                    // Share button
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(42.dp)
                            .tvFocusable(shape = CircleShape, onDpadCenterClick = onShare)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = shareMasterpieceTitle,
                            tint = Color.White
                        )
                    }

                    // Bookmark toggle
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(42.dp)
                            .tvFocusable(shape = CircleShape, onDpadCenterClick = onToggleFavorite)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = favoriteDesc,
                            tint = if (isFavorite) GoldAccent else Color.White
                        )
                    }
                }
            }

            // Interactive Zoomable Masterpiece Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black)
                    .clip(RoundedCornerShape(0.dp))
                    .transformable(state = transformState)
                    .testTag("detail_zoomable_canvas"),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = artwork.bestDisplayImage,
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )

                // Reset zoom pill if zoomed in
                if (scale > 1.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, GoldAccent, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                scale = 1f
                                offset = Offset.Zero
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val resetZoomLabel = stringResource(R.string.reset_zoom)
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = resetZoomLabel,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${String.format("%.1fx", scale)} ($resetZoomLabel)",
                                fontSize = 11.sp,
                                fontFamily = OutfitFontFamily,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Pinch zoom hint
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = pinchToInspectText,
                                fontSize = 10.sp,
                                fontFamily = OutfitFontFamily,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metadata & Analysis Details
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = artwork.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = ParchmentWhite
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

                Spacer(modifier = Modifier.height(14.dp))

                // Tag Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(label = artwork.movement)
                    if (artwork.objectDate.isNotBlank()) {
                        InfoChip(label = artwork.objectDate)
                    }
                    InfoChip(label = cc0PublicDomainText)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Artist Biography Card
                if (artwork.artistDisplayBio.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = artistBiographyText,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GoldAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = artwork.artistDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = PlayfairFontFamily,
                                color = ParchmentWhite
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = artwork.artistDisplayBio,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ParchmentWhite.copy(alpha = 0.9f),
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Curatorial Story Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = historicalContextText,
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldAccent
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val defaultCuratorNote = stringResource(R.string.default_curator_note_detail)
                        val defaultMedium = stringResource(R.string.default_medium)
                        val defaultDimensions = stringResource(R.string.default_dimensions)
                        val defaultCredit = stringResource(R.string.default_credit)
                        val defaultDepartment = stringResource(R.string.default_department)

                        Text(
                            text = artwork.curatorNote.ifBlank { defaultCuratorNote },
                            style = MaterialTheme.typography.bodyLarge,
                            color = ParchmentWhite.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ProvenanceRow(mediumText, artwork.medium.ifBlank { defaultMedium })
                        ProvenanceRow(dimensionsText, artwork.dimensions.ifBlank { defaultDimensions })
                        ProvenanceRow(creditText, artwork.creditLine.ifBlank { defaultCredit })
                        ProvenanceRow(departmentText, artwork.department.ifBlank { defaultDepartment })
                        ProvenanceRow(sourceText, artwork.sourceMuseum)
                    }
                }
            }
        }

        // Sticky Bottom Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = GalleryBlack.copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenWallpaperModal,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .tvFocusable(shape = RoundedCornerShape(14.dp), onDpadCenterClick = onOpenWallpaperModal)
                        .testTag("detail_set_wallpaper_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = GalleryBlack
                    ),
                    shape = RoundedCornerShape(14.dp)
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
                        .height(52.dp)
                        .tvFocusable(shape = RoundedCornerShape(14.dp), onDpadCenterClick = onDownload)
                        .testTag("detail_download_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ParchmentWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    shape = RoundedCornerShape(14.dp),
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
            }
        }
    }
}
