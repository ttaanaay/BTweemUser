package com.btweeu.app.ui.addedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btweeu.app.R
import com.btweeu.app.ui.components.AutocompleteTextField
import com.btweeu.app.ui.components.CategoryDropdown
import com.btweeu.app.ui.components.LoginRequiredDialog
import com.btweeu.app.ui.components.QuoteImagePicker
import com.btweeu.app.ui.components.TagChipInput
import com.btweeu.app.ui.util.sourceTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    onDone: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val sourceTypeOptions by viewModel.sourceTypeOptions.collectAsStateWithLifecycle()
    val categoryOptions by viewModel.categoryOptions.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.didSave, state.errorMessage) {
        val error = state.errorMessage
        if (state.didSave) {
            onDone()
        } else if (error != null) {
            snackbarHostState.showSnackbar(error)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_edit_title_add)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::onSave, enabled = !state.isSaving) {
                        Text(stringResource(R.string.action_save))
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::onTextChanged,
                label = { Text(stringResource(R.string.add_edit_label_text)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            AutocompleteTextField(
                value = state.sourceTitle,
                onValueChange = viewModel::onSourceTitleChanged,
                label = stringResource(R.string.add_edit_label_source_title),
                suggestions = suggestions.sourceTitles,
                modifier = Modifier.fillMaxWidth()
            )
            SourceTypeDropdown(
                selected = state.sourceType,
                options = sourceTypeOptions,
                onSelected = viewModel::onSourceTypeChanged
            )
            AutocompleteTextField(
                value = state.speaker,
                onValueChange = viewModel::onSpeakerChanged,
                label = stringResource(R.string.add_edit_label_speaker),
                suggestions = suggestions.speakers,
                modifier = Modifier.fillMaxWidth()
            )
            AutocompleteTextField(
                value = state.author,
                onValueChange = viewModel::onAuthorChanged,
                label = stringResource(R.string.add_edit_label_author),
                suggestions = suggestions.authors,
                modifier = Modifier.fillMaxWidth()
            )
            CategoryDropdown(
                options = categoryOptions,
                selected = state.category,
                onSelected = viewModel::onCategoryChanged
            )
            TagChipInput(
                value = state.tagsInput,
                onValueChange = viewModel::onTagsInputChanged,
                label = stringResource(R.string.add_edit_label_tags),
                suggestions = suggestions.tags,
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text(
                    text = stringResource(R.string.add_edit_visibility_label),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.visibility == "PUBLIC",
                        onClick = { viewModel.onVisibilityChanged("PUBLIC") },
                        label = { Text(stringResource(R.string.visibility_public)) },
                        leadingIcon = { Icon(Icons.Filled.Public, contentDescription = null) }
                    )
                    FilterChip(
                        selected = state.visibility == "PRIVATE",
                        onClick = { viewModel.onVisibilityChanged("PRIVATE") },
                        label = { Text(stringResource(R.string.visibility_private)) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) }
                    )
                }
                Text(
                    text = if (state.visibility == "PRIVATE") {
                        stringResource(R.string.add_edit_visibility_private_hint)
                    } else {
                        stringResource(R.string.add_edit_visibility_public_hint)
                    },
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            QuoteImagePicker(
                imageUrl = state.imageUrl,
                isUploading = state.isUploadingImage,
                onImagePicked = viewModel::onImagePicked,
                onRemoveImage = viewModel::onRemoveImage
            )
        }
    }

    if (state.needsLogin) {
        LoginRequiredDialog(
            onDismiss = viewModel::consumeNeedsLogin,
            onLogIn = {
                viewModel.consumeNeedsLogin()
                onNavigateToLogin()
            }
        )
    }
}

/**
 * A dropdown built from plain, long-stable APIs (Box + clickable overlay + DropdownMenu)
 * rather than the Material3 ExposedDropdownMenuBox family, whose menuAnchor()/
 * ExposedDropdownMenu API surface has shifted across recent Material3 releases.
 */
@Composable
private fun SourceTypeDropdown(selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = sourceTypeLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.add_edit_label_source_type)) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { type ->
                DropdownMenuItem(
                    text = { Text(sourceTypeLabel(type)) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
