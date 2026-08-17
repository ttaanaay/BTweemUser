package com.btweeu.app.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Must match IconCatalog.KEYS on the server and the icon maps in the admin panel/web app. */
fun categoryIconFor(key: String?): ImageVector = when (key) {
    "sun" -> Icons.Filled.WbSunny
    "heart" -> Icons.Filled.Favorite
    "star" -> Icons.Filled.Star
    "groups" -> Icons.Filled.Groups
    "brain" -> Icons.Filled.Psychology
    "laugh" -> Icons.Filled.EmojiEmotions
    "book" -> Icons.Filled.MenuBook
    "movie" -> Icons.Filled.Movie
    "music" -> Icons.Filled.MusicNote
    "coffee" -> Icons.Filled.LocalCafe
    "moon" -> Icons.Filled.NightsStay
    "trophy" -> Icons.Filled.EmojiEvents
    "flame" -> Icons.Filled.LocalFireDepartment
    "leaf" -> Icons.Filled.Eco
    "compass" -> Icons.Filled.Explore
    "sparkle" -> Icons.Filled.AutoAwesome
    "water" -> Icons.Filled.WaterDrop
    "waves" -> Icons.Filled.Waves
    "mountain" -> Icons.Filled.Terrain
    "home" -> Icons.Filled.Home
    "handshake" -> Icons.Filled.Handshake
    "flight" -> Icons.Filled.FlightTakeoff
    "work" -> Icons.Filled.Work
    "money" -> Icons.Filled.Paid
    "health" -> Icons.Filled.HealthAndSafety
    "sport" -> Icons.Filled.SportsSoccer
    "palette" -> Icons.Filled.Palette
    "peace" -> Icons.Filled.SelfImprovement
    "idea" -> Icons.Filled.Lightbulb
    "gift" -> Icons.Filled.CardGiftcard
    "growth" -> Icons.Filled.TrendingUp
    "shield" -> Icons.Filled.Shield
    else -> Icons.Filled.Label
}
