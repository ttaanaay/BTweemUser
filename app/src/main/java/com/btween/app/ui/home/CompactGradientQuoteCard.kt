package com.btween.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.btween.app.domain.model.SocialQuote
import com.btween.app.ui.theme.QuoteSerifFontFamily

/**
 * Compact gradient-backed quote card used for both the "Trending" horizontal row and the
 * "Recently Approved" grid on Home - same visual treatment, different layout containers.
 */
@Composable
fun CompactGradientQuoteCard(
    quote: SocialQuote,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
    onQuoteClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(150.dp)
            .fillMaxWidth()
            .clickable(onClick = onQuoteClick)
            .background(gradientForSeed(quote.id + 1), MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "\u201C${quote.text}\u201D",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = QuoteSerifFontFamily,
                    fontStyle = FontStyle.Italic
                ),
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f, fill = false))
        }

        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = "\u2014 ${quote.speaker}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable(onClick = onToggleLike),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (quote.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (quote.isLikedByMe) MaterialTheme.colorScheme.error else Color.White,
                modifier = Modifier
                    .size(16.dp)
            )
            Text(
                text = " ${quote.likeCount}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}
