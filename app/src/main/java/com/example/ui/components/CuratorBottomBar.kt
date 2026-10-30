package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.viewmodel.CuratorScreen
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.tv.tvFocusable

@Composable
fun CuratorBottomBar(
    currentScreen: CuratorScreen,
    onNavigate: (CuratorScreen) -> Unit
) {
    val dailyLabel = stringResource(R.string.nav_daily)
    val exploreLabel = stringResource(R.string.nav_explore)
    val salonLabel = stringResource(R.string.nav_salon)

    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("curator_bottom_nav_bar"),
        containerColor = GalleryBlack,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(CuratorScreen.DAILY, dailyLabel, Pair(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)),
            Triple(CuratorScreen.GALLERY, exploreLabel, Pair(Icons.Filled.Collections, Icons.Outlined.Collections)),
            Triple(CuratorScreen.FAVORITES, salonLabel, Pair(Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder))
        )

        items.forEach { (screen, label, icons) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GalleryBlack,
                    selectedTextColor = GoldAccent,
                    indicatorColor = GoldAccent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .tvFocusable(shape = RoundedCornerShape(16.dp), onDpadCenterClick = { onNavigate(screen) })
                    .testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
