package com.btween.app.ui.settings

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.BuildConfig
import com.btween.app.R
import com.btween.app.domain.model.AppLanguage
import com.btween.app.domain.model.ThemeMode
import com.btween.app.ui.components.PasswordTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isDeletingAccount by viewModel.isDeletingAccount.collectAsStateWithLifecycle()
    val deleteAccountError by viewModel.deleteAccountError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val emailVerification by viewModel.emailVerification.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(deleteAccountError) {
        deleteAccountError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeDeleteAccountError()
        }
    }
    LaunchedEffect(emailVerification.infoMessage) {
        emailVerification.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeInfoMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (!emailVerification.isEmailVerified) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Verify your email",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "Confirm your email address to secure your account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                        TextButton(onClick = viewModel::onShowVerifyDialog) {
                            Text("Verify now")
                        }
                    }
                }
            }

            SettingsSectionTitle(stringResource(R.string.settings_section_appearance))

            ThemeMode.entries.forEach { mode ->
                ThemeOptionRow(
                    label = mode.label(),
                    selected = settings.themeMode == mode,
                    onSelect = { viewModel.onThemeModeSelected(mode) }
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(R.string.settings_dynamic_color_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.settings_dynamic_color_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.useDynamicColor,
                        onCheckedChange = viewModel::onDynamicColorToggled
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionTitle(stringResource(R.string.settings_section_language))

            AppLanguage.entries.forEach { language ->
                ThemeOptionRow(
                    label = language.label(),
                    selected = appLanguage == language,
                    onSelect = {
                        viewModel.onLanguageSelected(language)
                        // MainActivity is a plain ComponentActivity, not AppCompatActivity -
                        // AppCompat's automatic recreate-on-locale-change isn't guaranteed to
                        // kick in reliably in that case (especially pre-API 33), so trigger it
                        // explicitly to make sure the change actually takes visible effect.
                        (context as? Activity)?.recreate()
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionTitle(stringResource(R.string.settings_section_about))
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.settings_about_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsSectionTitle(stringResource(R.string.settings_section_account))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = false, onClick = onChangePasswordClick)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text("Change password")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = false, onClick = { showLogoutDialog = true })
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    stringResource(R.string.settings_logout),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = false, onClick = { showDeleteAccountDialog = true })
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    "Delete account",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.settings_logout_dialog_title)) },
            text = { Text(stringResource(R.string.settings_logout_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.onLogout(onDone = onLoggedOut)
                }) { Text(stringResource(R.string.settings_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        var deletePassword by remember { mutableStateOf("") }
        val hasPassword by viewModel.hasPassword.collectAsStateWithLifecycle()
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            title = { Text("Delete your account?") },
            text = {
                Column {
                    Text(
                        "This permanently deletes your profile, quotes, comments, likes, follows, " +
                            "and collections. This can't be undone."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Accounts signed in via Google/Facebook/Microsoft never set a password,
                    // so there's nothing to confirm with - being signed in is already the
                    // strongest check available for them.
                    if (hasPassword != false) {
                        PasswordTextField(
                            value = deletePassword,
                            onValueChange = { deletePassword = it },
                            label = "Enter your password to confirm",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteAccount(password = deletePassword, onDeleted = {
                            showDeleteAccountDialog = false
                        })
                    },
                    enabled = !isDeletingAccount && (hasPassword == false || deletePassword.isNotBlank())
                ) {
                    Text(
                        if (isDeletingAccount) "Deleting..." else "Delete permanently",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isDeletingAccount
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (emailVerification.showVerifyDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissVerifyDialog,
            title = { Text("Verify your email") },
            text = {
                Column {
                    Text("Enter the code we sent to your email.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailVerification.code,
                        onValueChange = viewModel::onCodeChanged,
                        label = { Text("6-digit code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(onClick = viewModel::onResendCode, enabled = !emailVerification.isResending) {
                        Text(if (emailVerification.isResending) "Sending..." else "Resend code")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onSubmitCode,
                    enabled = !emailVerification.isSubmitting && emailVerification.code.isNotBlank()
                ) { Text("Verify") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissVerifyDialog) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
}

@Composable
private fun AppLanguage.label(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
    AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
    AppLanguage.THAI -> stringResource(R.string.settings_language_thai)
}
