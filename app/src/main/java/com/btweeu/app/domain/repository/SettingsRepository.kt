package com.btweeu.app.domain.repository

import com.btweeu.app.domain.model.SortOrder
import com.btweeu.app.domain.model.ThemeMode
import com.btweeu.app.domain.model.UserSettings
import com.btweeu.app.domain.model.ViewMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val userSettings: Flow<UserSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setUseDynamicColor(enabled: Boolean)

    suspend fun setLibraryViewMode(mode: ViewMode)

    suspend fun setLibrarySortOrder(order: SortOrder)

    suspend fun setOnboardingComplete(completed: Boolean)
}
