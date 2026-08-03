package com.btween.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.btween.app.R

/**
 * Shown when a signed-out guest taps an action that needs an account (like, follow, comment,
 * post, save to a collection, etc). Keeping this as one shared component means every screen
 * gets the same wording and behavior rather than each hand-rolling its own prompt.
 */
@Composable
fun LoginRequiredDialog(
    onDismiss: () -> Unit,
    onLogIn: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.guest_login_required_title)) },
        text = { Text(stringResource(R.string.guest_login_required_message)) },
        confirmButton = {
            TextButton(onClick = onLogIn) { Text(stringResource(R.string.auth_log_in)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
