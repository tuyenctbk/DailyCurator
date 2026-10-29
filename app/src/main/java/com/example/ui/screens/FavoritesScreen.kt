package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Artwork
import com.example.ui.components.ArtworkCard
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.PlayfairFontFamily

import com.example.ui.components.donation.SupporterBadge
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.IconButton

@Composable
fun FavoritesScreen(
    favorites: List<Artwork>,
    isSupporter: Boolean,
    onSelectArtwork: (Artwork) -> Unit,
    onToggleFavorite: (Artwork) -> Unit,
    onExploreArchive: () -> Unit,
    onOpenDonationModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myPrivateSalonText = stringResource(R.string.my_private_salon)
    val salonSubtitleText = stringResource(R.string.salon_subtitle)
    val salonEmptyText = stringResource(R.string.salon_empty)
    val salonEmptySubtitleText = stringResource(R.string.salon_empty_subtitle)
    val exploreMasterworksText = stringResource(R.string.explore_masterworks)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalleryBlack)
            .padding(horizontal = 16.dp)
            .testTag("favorites_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = myPrivateSalonText,
                    style = MaterialTheme.typography.displayMedium,
                    color = ParchmentWhite
                )
                Text(
                    text = salonSubtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSupporter) {
                    SupporterBadge()
                } else {
                    IconButton(
                        onClick = onOpenDonationModal,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = stringResource(R.string.donation_title),
                            tint = GoldAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (favorites.isNotEmpty()) {
            Text(
                text = stringResource(R.string.masterpieces_saved_format, favorites.size),
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GoldAccent
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = GoldAccent.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = salonEmptyText,
                        fontFamily = PlayfairFontFamily,
                        fontSize = 22.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = salonEmptySubtitleText,
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        color = ParchmentMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onExploreArchive,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = GalleryBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("explore_from_favorites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = exploreMasterworksText,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { artwork ->
                    ArtworkCard(
                        artwork = artwork,
                        isFavorite = true,
                        onSelect = { onSelectArtwork(artwork) },
                        onToggleFavorite = { onToggleFavorite(artwork) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
