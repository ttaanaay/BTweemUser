package com.btweeu.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Compose's `LocalContext.current` isn't always the raw Activity instance - it can be
 * wrapped by a ContextThemeWrapper or similar decorator, in which case a direct
 * `context as? Activity` cast silently returns null with no error. This walks the
 * ContextWrapper chain to find the actual Activity underneath.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
