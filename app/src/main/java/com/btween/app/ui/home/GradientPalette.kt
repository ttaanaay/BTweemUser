package com.btween.app.ui.home

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A small set of hand-picked gradient pairs in the app's amber/charcoal editorial palette.
 * Used as quote card backgrounds in place of photos (a future enhancement once image
 * upload/hosting exists - for now every card gets one of these instead).
 */
private val gradientPairs = listOf(
    Color(0xFF3A2A1F) to Color(0xFF6B4423), // warm amber-brown
    Color(0xFF1F2A3A) to Color(0xFF2E4A6B), // deep blue
    Color(0xFF2A1F3A) to Color(0xFF5A3A7A), // plum
    Color(0xFF1F3A2E) to Color(0xFF2E6B4A), // forest green
    Color(0xFF3A1F2A) to Color(0xFF7A2E4A), // maroon-rose
    Color(0xFF2A2A1F) to Color(0xFF6B6423)  // olive-gold
)

/** Deterministic pick so the same quote id always renders with the same gradient. */
fun gradientForSeed(seed: Long): Brush {
    val (start, end) = gradientPairs[(seed.mod(gradientPairs.size.toLong())).toInt()]
    return Brush.linearGradient(listOf(start, end))
}
