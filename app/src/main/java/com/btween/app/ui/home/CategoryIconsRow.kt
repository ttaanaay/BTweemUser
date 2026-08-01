package com.btween.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.domain.repository.PublicCategory

// A fixed icon/color palette cycled by position, since admin-created categories don't carry
// their own icon or color - this keeps the row visually consistent with the original design
// (which had one icon per category) without needing an icon picker in the admin panel.
private val paletteIcons: List<ImageVector> = listOf(
    Icons.Filled.WbSunny,
    Icons.Filled.Favorite,
    Icons.Filled.Star,
    Icons.Filled.Groups,
    Icons.Filled.Psychology,
    Icons.Filled.EmojiEmotions,
    Icons.Filled.MenuBook,
    Icons.Filled.Movie
)
private val paletteColors: List<Color> = listOf(
    Color(0xFF5DA8E8),
    Color(0xFFE85D8A),
    Color(0xFFE8A94C),
    Color(0xFF4CAF7D),
    Color(0xFF8E7CC3),
    Color(0xFFE8C64C),
    Color(0xFF6FBF73),
    Color(0xFF4C7CE8)
)

/**
 * Category shortcuts on Home - tapping one opens [com.btween.app.ui.home.CategoryQuotesScreen]
 * filtered to that category via the feed API's `category` query param. The category list
 * itself comes from the server (admin-managed), not a hardcoded set - icon/color are just
 * assigned by position in a repeating palette since categories are plain names server-side.
 */
@Composable
fun CategoryIconsRow(
    onCategoryClick: (String) -> Unit,
    viewModel: CategoryIconsViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    if (categories.isEmpty()) return

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryChipItem(category = category, onClick = { onCategoryClick(category.name) })
        }
    }
}

@Composable
private fun CategoryChipItem(category: PublicCategory, onClick: () -> Unit) {
    val index = (category.id % paletteIcons.size).toInt()
    val icon = paletteIcons.getOrElse(index) { Icons.Filled.Label }
    val color = paletteColors.getOrElse(index) { MaterialTheme.colorScheme.primary }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
