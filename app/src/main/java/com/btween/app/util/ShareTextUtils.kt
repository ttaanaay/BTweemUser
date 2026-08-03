package com.btween.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.SocialQuote

private fun buildShareText(text: String, speaker: String, sourceTitle: String, link: String? = null): String = buildString {
    append("\u201C$text\u201D")
    append("\n\u2014 $speaker")
    if (sourceTitle.isNotBlank()) append(", $sourceTitle")
    if (link != null) append("\n\n$link")
}

/** Local-only quotes have nothing to link to - a person's own device library isn't
 * reachable from anywhere else, so these are shared as plain text. */
fun buildShareText(quote: Quote): String = buildShareText(quote.text, quote.speaker, quote.sourceTitle)

/** Published quotes get a deep link appended - tapping it in another app (or pasting it
 * into a browser with the app installed) opens straight to this quote in BTween. */
fun buildShareText(quote: SocialQuote): String =
    buildShareText(quote.text, quote.speaker, quote.sourceTitle, link = "btween://quote/${quote.id}")

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    val clip = ClipData.newPlainText("Quote", text)
    clipboardManager?.setPrimaryClip(clip)
}

fun copyQuoteToClipboard(context: Context, quote: Quote) = copyToClipboard(context, buildShareText(quote))
fun copyQuoteToClipboard(context: Context, quote: SocialQuote) = copyToClipboard(context, buildShareText(quote))

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share quote"))
}

fun shareQuoteAsText(context: Context, quote: Quote) = shareText(context, buildShareText(quote))
fun shareQuoteAsText(context: Context, quote: SocialQuote) = shareText(context, buildShareText(quote))

/** Shares just the profile deep link (e.g. from a "share my profile" action) - not tied to
 * any specific quote. */
fun shareProfileLink(context: Context, userId: Long, displayName: String) {
    shareText(context, "Check out $displayName on BTween\nbtween://profile/$userId")
}

