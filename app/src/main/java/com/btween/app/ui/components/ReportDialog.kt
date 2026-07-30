package com.btween.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.domain.repository.ReportReason
import com.btween.app.domain.repository.ReportTargetType

private fun ReportReason.label(): String = when (this) {
    ReportReason.SPAM -> "Spam"
    ReportReason.HARASSMENT -> "Harassment or bullying"
    ReportReason.INAPPROPRIATE -> "Inappropriate content"
    ReportReason.MISINFORMATION -> "Misinformation"
    ReportReason.OTHER -> "Other"
}

/**
 * Self-contained report dialog: owns its own ViewModel instance, submits, and shows a brief
 * confirmation before calling [onDismiss]. Drop this in wherever a report action is needed
 * (a quote's "..." menu, a profile's overflow menu, etc.) guarded by a boolean show/hide flag.
 */
@Composable
fun ReportDialog(
    targetType: ReportTargetType,
    targetId: Long,
    onDismiss: () -> Unit,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var details by remember { mutableStateOf("") }

    LaunchedEffect(uiState.didSubmit) {
        if (uiState.didSubmit) onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (targetType == ReportTargetType.QUOTE) "Report quote" else "Report user") },
        text = {
            Column {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator()
                } else {
                    ReportReason.entries.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedReason == reason,
                                    onClick = { selectedReason = reason }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                            Text(reason.label())
                        }
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Additional details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    uiState.errorMessage?.let {
                        Text(it)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedReason?.let { reason ->
                        viewModel.onSubmit(targetType, targetId, reason, details.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = !uiState.isSubmitting && selectedReason != null
            ) { Text("Submit") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isSubmitting) { Text("Cancel") }
        }
    )
}
