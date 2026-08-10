package com.btween.app.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.btween.app.domain.model.AppLanguage
import java.util.Locale

private const val PREFS_NAME = "btween_locale_prefs"
private const val KEY_LANGUAGE = "language"

/**
 * Applies the user's language choice by hand via SharedPreferences + Configuration, rather
 * than relying on AppCompatDelegate.setApplicationLocales() as the source of truth.
 *
 * AppCompatDelegate's per-app language API is designed around AppCompatActivity's own
 * attachBaseContext hook and an internal ContentProvider-backed store; in practice, reading
 * it back via getApplicationLocales() from a plain ComponentActivity (like MainActivity
 * here) was unreliable - the choice would appear to revert after Activity.recreate(). A
 * plain SharedPreferences value read directly in attachBaseContext sidesteps that
 * unreliability entirely and is the actual source of truth here.
 *
 * setApplicationLocales() is still called too (best-effort) purely so the choice also shows
 * up in the system's own per-app language settings screen on API 33+ (enabled via
 * `android:localeConfig` in the manifest) - just not depended on for correctness.
 */
object LocaleManager {

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun applyLanguage(context: Context, language: AppLanguage) {
        prefs(context).edit().putString(KEY_LANGUAGE, language.name).apply()

        val localeList = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            AppLanguage.THAI -> LocaleListCompat.forLanguageTags("th")
        }
        runCatching { AppCompatDelegate.setApplicationLocales(localeList) }
    }

    fun currentLanguage(context: Context): AppLanguage {
        val stored = prefs(context).getString(KEY_LANGUAGE, null)
        return runCatching { stored?.let { AppLanguage.valueOf(it) } }.getOrNull() ?: AppLanguage.SYSTEM
    }

    /**
     * Manually applies the persisted language choice to [base]'s Configuration/Resources.
     * Call this from `Activity.attachBaseContext()`. For [AppLanguage.SYSTEM], returns
     * [base] unchanged so the device's own locale is used.
     */
    fun wrapContext(base: Context): Context {
        val language = currentLanguage(base)
        if (language == AppLanguage.SYSTEM) return base

        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale("en")
            AppLanguage.THAI -> Locale("th")
            AppLanguage.SYSTEM -> return base
        }
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
