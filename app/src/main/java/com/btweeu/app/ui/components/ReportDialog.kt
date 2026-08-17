package com.btweeu.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btweeu.app.R
import com.btweeu.app.domain.repository.ReportReason
import com.btweeu.app.domain.repository.ReportTargetType

@Composable
private fun ReportReason.label(): String = when (this) {
    ReportReason.SPAM -> stringResource(R.string.report_reason_spam)
    ReportReason.HARASSMENT -> stringResource(R.string.report_reason_harassment)
    ReportReason.INAPPROPRIATE -> stringResource(R.string.report_reason_inappropriate)
    ReportReason.MISINFORMATION -> stringResource(R.string.report_reason_misinformation)
    ReportReason.OTHER -> stringResource(R.string.report_reason_other)
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
        title = {
            Text(
                when (targetType) {
                    ReportTargetType.QUOTE -> stringResource(R.string.report_title_quote)
                    ReportTargetType.COMMENT -> stringResource(R.string.report_title_comment)
                    ReportTargetType.USER -> stringResource(R.string.report_title_user)
                }
            )
        },
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
                        label = { Text(stringResource(R.string.report_label_details)) },
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
            ) { Text(stringResource(R.string.report_action_submit)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isSubmitting) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
