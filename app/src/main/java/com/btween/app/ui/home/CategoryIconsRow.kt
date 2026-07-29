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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private data class CategoryChip(val label: String, val icon: ImageVector, val color: Color)

private val categoryChips = listOf(
    CategoryChip("Life", Icons.Filled.WbSunny, Color(0xFF5DA8E8)),
    CategoryChip("Love", Icons.Filled.Favorite, Color(0xFFE85D8A)),
    CategoryChip("Motivation", Icons.Filled.Star, Color(0xFFE8A94C)),
    CategoryChip("Success", Icons.Filled.Groups, Color(0xFF4CAF7D)),
    CategoryChip("Wisdom", Icons.Filled.Psychology, Color(0xFF8E7CC3)),
    CategoryChip("Humor", Icons.Filled.EmojiEmotions, Color(0xFFE8C64C)),
    CategoryChip("Books", Icons.Filled.MenuBook, Color(0xFF6FBF73)),
    CategoryChip("Movie", Icons.Filled.Movie, Color(0xFF4C7CE8))
)

/**
 * Category shortcuts on Home - tapping one opens [com.btween.app.ui.home.CategoryQuotesScreen]
 * filtered to that category via the feed API's `category` query param.
 */
@Composable
fun CategoryIconsRow(onCategoryClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categoryChips) { chip ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onCategoryClick(chip.label) }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(chip.color.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = chip.icon, contentDescription = null, tint = chip.color)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = chip.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
