package com.btweeu.app.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.btweeu.app.domain.repository.ReportReason
import com.btweeu.app.domain.repository.ReportRepository
import com.btweeu.app.domain.repository.ReportTargetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val isSubmitting: Boolean = false,
    val didSubmit: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSubmitted() {
        _uiState.value = ReportUiState()
    }

    fun onSubmit(targetType: ReportTargetType, targetId: Long, reason: ReportReason, details: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            reportRepository.submitReport(targetType, targetId, reason, details)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, didSubmit = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = error.message)
                }
        }
    }
}
