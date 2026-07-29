package com.btween.app.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.btween.app.domain.model.AppLanguage
import java.util.Locale

/**
 * Wraps AndroidX's per-app language API (`AppCompatDelegate.setApplicationLocales`).
 *
 * This works even though the app uses plain `ComponentActivity` rather than
 * `AppCompatActivity`: since appcompat 1.6.0, simply having the appcompat dependency
 * present is enough - it registers a ContentProvider at process start that reads the
 * persisted locale choice and applies + auto-recreates activities for you, on every
 * API level back to 24. On API 33+ it additionally delegates to the platform
 * `LocaleManager` so the choice also shows up in the system's own per-app language
 * settings screen (enabled via `android:localeConfig` in the manifest).
 *
 * In practice, that auto-recreate path is only reliable for `AppCompatActivity` (it hooks
 * into AppCompat's own `attachBaseContext` override). For a plain `ComponentActivity` like
 * `MainActivity`, [wrapContext] below does the equivalent by hand.
 */
object LocaleManager {

    fun applyLanguage(language: AppLanguage) {
        val localeList = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            AppLanguage.THAI -> LocaleListCompat.forLanguageTags("th")
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun currentLanguage(): AppLanguage {
        val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return when {
            tags.startsWith("th") -> AppLanguage.THAI
            tags.startsWith("en") -> AppLanguage.ENGLISH
            else -> AppLanguage.SYSTEM
        }
    }

    /**
     * Manually applies the persisted language choice to [base]'s Configuration/Resources.
     * Call this from `Activity.attachBaseContext()`. For [AppLanguage.SYSTEM], returns
     * [base] unchanged so the device's own locale is used.
     */
    fun wrapContext(base: Context): Context {
        val language = currentLanguage()
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
