package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Artwork
import com.example.ui.components.ArtworkCard
import com.example.ui.theme.DividerDark
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.PlayfairFontFamily
import com.example.ui.theme.SurfaceDark

@Composable
fun GalleryBrowseScreen(
    artworks: List<Artwork>,
    favoriteIds: Set<String>,
    searchQuery: String,
    selectedMovement: String,
    selectedEra: String,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onMovementSelected: (String) -> Unit,
    onEraSelected: (String) -> Unit,
    onSelectArtwork: (Artwork) -> Unit,
    onToggleFavorite: (Artwork) -> Unit,
    modifier: Modifier = Modifier
) {
    val movements = listOf(
        "All",
        "Impressionism",
        "Post-Impressionism",
        "Dutch Golden Age",
        "Ukiyo-e",
        "Romanticism",
        "Modern Realism"
    )

    val eras = listOf(
        "All",
        "19th Century",
        "17th Century",
        "Edo Period",
        "20th Century"
    )

    val fineArtArchiveText = stringResource(R.string.fine_art_archive)
    val archiveSubtitleText = stringResource(R.string.archive_subtitle)
    val searchPlaceholderText = stringResource(R.string.search_placeholder)
    val clearSearchText = stringResource(R.string.clear_search)
    val noPaintingsFoundText = stringResource(R.string.no_paintings_found)
    val noPaintingsSubtitleText = stringResource(R.string.no_paintings_subtitle)
    val resetFiltersText = stringResource(R.string.reset_filters)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GalleryBlack)
            .padding(horizontal = 16.dp)
            .testTag("gallery_browse_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Title Header
        Text(
            text = fineArtArchiveText,
            style = MaterialTheme.typography.displayMedium,
            color = ParchmentWhite
        )
        Text(
            text = archiveSubtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = ParchmentMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gallery_search_input"),
            placeholder = {
                Text(
                    text = searchPlaceholderText,
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = searchPlaceholderText,
                    tint = GoldAccent
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = GoldAccent,
                        strokeWidth = 2.dp
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChanged("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = clearSearchText,
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = DividerDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = GoldAccent
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Movement Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            movements.forEach { movement ->
                val isSelected = selectedMovement.equals(movement, ignoreCase = true)
                FilterPill(
                    label = movement,
                    isSelected = isSelected,
                    onClick = { onMovementSelected(movement) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Era Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            eras.forEach { era ->
                val isSelected = selectedEra.equals(era, ignoreCase = true)
                FilterPill(
                    label = era,
                    isSelected = isSelected,
                    onClick = { onEraSelected(era) },
                    isEra = true
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Result count label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.masterpieces_available_format, artworks.size),
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.5f)
            )

            if (selectedMovement != "All" || selectedEra != "All" || searchQuery.isNotEmpty()) {
                Text(
                    text = resetFiltersText,
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    modifier = Modifier
                        .clickable {
                            onQueryChanged("")
                            onMovementSelected("All")
                            onEraSelected("All")
                        }
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Artwork List
        if (artworks.isEmpty() && !isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = GoldAccent.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = noPaintingsFoundText,
                        fontFamily = PlayfairFontFamily,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = noPaintingsSubtitleText,
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        color = ParchmentMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = artworks,
                    key = { it.id }
                ) { artwork ->
                    ArtworkCard(
                        artwork = artwork,
                        isFavorite = favoriteIds.contains(artwork.id),
                        onSelect = { onSelectArtwork(artwork) },
                        onToggleFavorite = { onToggleFavorite(artwork) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    isEra: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) GoldAccent else SurfaceDark
            )
            .border(
                1.dp,
                if (isSelected) GoldAccent else DividerDark,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("filter_${label.lowercase().replace(" ", "_")}")
    ) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) GalleryBlack else Color.White.copy(alpha = 0.85f)
        )
    }
}
