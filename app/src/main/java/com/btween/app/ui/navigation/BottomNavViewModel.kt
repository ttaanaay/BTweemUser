package com.btween.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.btween.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BottomNavViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun getCurrentUserId(): Long? = authRepository.getCurrentUserId()
}
