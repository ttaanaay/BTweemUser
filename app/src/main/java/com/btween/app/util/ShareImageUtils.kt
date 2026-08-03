package com.btween.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import com.btween.app.domain.model.Quote
import com.btween.app.domain.model.SocialQuote
import java.io.File
import java.io.FileOutputStream

private const val IMAGE_WIDTH = 1080
private const val PADDING = 96f

/**
 * Renders the given quote text/attribution onto a card bitmap suitable for sharing to social
 * apps or messaging. If [backgroundBitmap] is provided (the quote's attached photo), it's
 * drawn cropped to fill the card with a dark scrim behind the text for readability; otherwise
 * falls back to a plain dark background. Sizing is dynamic: canvas height grows to fit the
 * wrapped quote text plus the attribution footer. Works from primitive fields so it's shared
 * between the local [Quote] model and the online [SocialQuote] model.
 */
private fun renderQuoteBitmap(text: String, speaker: String, sourceTitle: String, backgroundBitmap: Bitmap?): Bitmap {
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

    // With a background photo, give the text more breathing room (a fixed-height photo card)
    // rather than shrink-to-fit, since the photo itself needs a substantial visible area.
    val height = if (backgroundBitmap != null) {
        1350
    } else {
        (PADDING * 2 + quoteLayout.height + 48f + attributionLayout.height + 80f).toInt()
    }

    val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    if (backgroundBitmap != null) {
        // Center-crop the source photo to fully cover the card, same idea as Compose's
        // ContentScale.Crop.
        val scale = maxOf(IMAGE_WIDTH.toFloat() / backgroundBitmap.width, height.toFloat() / backgroundBitmap.height)
        val scaledWidth = backgroundBitmap.width * scale
        val scaledHeight = backgroundBitmap.height * scale
        val dx = (IMAGE_WIDTH - scaledWidth) / 2f
        val dy = (height - scaledHeight) / 2f
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(dx, dy)
        }
        canvas.drawBitmap(backgroundBitmap, matrix, null)
        // Dark scrim across the whole card so white text stays readable over any photo.
        canvas.drawRect(RectF(0f, 0f, IMAGE_WIDTH.toFloat(), height.toFloat()), Paint().apply {
            color = Color.parseColor("#66000000")
        })
    } else {
        canvas.drawColor(Color.parseColor("#121212"))
    }

    val textBlockHeight = quoteLayout.height + 48f + attributionLayout.height
    val textStartY = if (backgroundBitmap != null) (height - textBlockHeight) / 2f else PADDING

    canvas.save()
    canvas.translate(PADDING, textStartY)
    quoteLayout.draw(canvas)
    canvas.restore()

    canvas.save()
    canvas.translate(PADDING, textStartY + quoteLayout.height + 48f)
    attributionLayout.draw(canvas)
    canvas.restore()

    canvas.drawText("BTween", PADDING, height - 40f, watermarkPaint)

    return bitmap
}

private fun shareBitmap(context: Context, bitmap: Bitmap, fileIdForName: String, linkText: String? = null) {
    val directory = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(directory, "quote_${fileIdForName}_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        if (linkText != null) putExtra(Intent.EXTRA_TEXT, linkText)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share quote image"))
}

private suspend fun loadBitmap(context: Context, url: String?): Bitmap? {
    if (url.isNullOrBlank()) return null
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(url).allowHardware(false).build()
        val result = loader.execute(request).drawable
        result?.let { drawable ->
            val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun shareQuoteAsImage(context: Context, quote: Quote) {
    val bitmap = renderQuoteBitmap(quote.text, quote.speaker, quote.sourceTitle, backgroundBitmap = null)
    shareBitmap(context, bitmap, quote.id.toString())
}

suspend fun shareQuoteAsImage(context: Context, quote: SocialQuote) {
    val bg = loadBitmap(context, quote.imageUrl)
    val bitmap = renderQuoteBitmap(quote.text, quote.speaker, quote.sourceTitle, bg)
    shareBitmap(context, bitmap, quote.id.toString(), linkText = "btween://quote/${quote.id}")
}
