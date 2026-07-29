package com.btween.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.SocialQuote

private fun buildShareText(text: String, speaker: String, sourceTitle: String): String = buildString {
    append("\u201C$text\u201D")
    append("\n\u2014 $speaker")
    if (sourceTitle.isNotBlank()) append(", $sourceTitle")
}

fun buildShareText(quote: Quote): String = buildShareText(quote.text, quote.speaker, quote.sourceTitle)
fun buildShareText(quote: SocialQuote): String = buildShareText(quote.text, quote.speaker, quote.sourceTitle)

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
