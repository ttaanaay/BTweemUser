package com.btween.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.SocialQuote
import java.io.File
import java.io.FileOutputStream

private const val IMAGE_WIDTH = 1080
private const val PADDING = 96f

/**
 * Renders the given quote text/attribution onto a dark, editorial-style card bitmap suitable
 * for sharing to social apps or messaging. Sizing is dynamic: the canvas height grows to fit
 * the wrapped quote text plus the attribution footer. Works from primitive fields so it's
 * shared between the local [Quote] model and the online [SocialQuote] model.
 */
private fun renderQuoteBitmap(text: String, speaker: String, sourceTitle: String): Bitmap {
    val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 56f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    val attributionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0A868")
        textSize = 34f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }
    val watermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#66FFFFFF")
        textSize = 28f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

    val textWidth = (IMAGE_WIDTH - PADDING * 2).toInt()
    val quoteText = "\u201C$text\u201D"

    val quoteLayout = StaticLayout.Builder
        .obtain(quoteText, 0, quoteText.length, quotePaint, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(8f, 1.15f)
        .build()

    val attributionText = buildString {
        append("\u2014 $speaker")
        if (sourceTitle.isNotBlank()) append(", $sourceTitle")
    }
    val attributionLayout = StaticLayout.Builder
        .obtain(attributionText, 0, attributionText.length, attributionPaint, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .build()

    val height = (PADDING * 2 + quoteLayout.height + 48f + attributionLayout.height + 80f).toInt()

    val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.parseColor("#121212"))

    canvas.save()
    canvas.translate(PADDING, PADDING)
    quoteLayout.draw(canvas)
    canvas.restore()

    canvas.save()
    canvas.translate(PADDING, PADDING + quoteLayout.height + 48f)
    attributionLayout.draw(canvas)
    canvas.restore()

    canvas.drawText("BTween", PADDING, height - 40f, watermarkPaint)

    return bitmap
}

private fun shareBitmap(context: Context, bitmap: Bitmap, fileIdForName: String) {
    val directory = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(directory, "quote_${fileIdForName}_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share quote image"))
}

fun shareQuoteAsImage(context: Context, quote: Quote) {
    val bitmap = renderQuoteBitmap(quote.text, quote.speaker, quote.sourceTitle)
    shareBitmap(context, bitmap, quote.id.toString())
}

fun shareQuoteAsImage(context: Context, quote: SocialQuote) {
    val bitmap = renderQuoteBitmap(quote.text, quote.speaker, quote.sourceTitle)
    shareBitmap(context, bitmap, quote.id.toString())
}
