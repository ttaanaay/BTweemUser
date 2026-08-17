package com.btweeu.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.btweeu.app.R
import com.btweeu.app.domain.model.SocialQuote
import com.btweeu.app.ui.theme.QuoteSerifFontFamily

/**
 * The "Daily Quote" spotlight card at the top of Home - the most-liked quote currently in
 * the public feed, on a gradient background (see GradientPalette.kt for why not a photo).
 */
@Composable
fun HeroQuoteCard(
    quote: SocialQuote,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
    onQuoteClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onQuoteClick)
            .clip(MaterialTheme.shapes.large)
            .then(
                if (quote.imageUrl.isNullOrBlank()) Modifier.background(gradientForSeed(quote.id)) else Modifier
            )
    ) {
        if (!quote.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = quote.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Dark scrim so white text stays readable over an arbitrary user photo.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Icon(
            imageVector = Icons.Filled.FormatQuote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.size(40.dp)
        )

        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                text = quote.text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = QuoteSerifFontFamily,
                    fontStyle = FontStyle.Italic
                ),
                color = Color.White,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u2014 ${quote.speaker}",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                    IconButton(onClick = onToggleLike) {
                        Icon(
                            imageVector = if (quote.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (quote.isLikedByMe) MaterialTheme.colorScheme.error else Color.White
                        )
                    }
                    Text(
                        text = quote.likeCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.TopEnd),
            shape = MaterialTheme.shapes.small,
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Text(
                text = stringResource(R.string.home_daily_quote_badge),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        }
    }
}
