package com.btweeu.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btweeu.app.R
import com.btweeu.app.ui.components.AutocompleteTextField
import com.btweeu.app.ui.components.CategoryDropdown
import com.btweeu.app.ui.components.QuoteImagePicker
import com.btweeu.app.ui.components.TagChipInput
import com.btweeu.app.ui.util.sourceTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSocialQuoteScreen(
    onDone: () -> Unit,
    viewModel: EditSocialQuoteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sourceTypeOptions by viewModel.sourceTypeOptions.collectAsStateWithLifecycle()
    val categoryOptions by viewModel.categoryOptions.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSourceTypeMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.didSave) {
        if (state.didSave) onDone()
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_quote_title)) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::onSave, enabled = !state.isSaving && !state.isLoading) {
                        Text(stringResource(R.string.edit_quote_action_save))
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
                label = { Text(stringResource(R.string.edit_quote_label_text)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            QuoteImagePicker(
                imageUrl = state.imageUrl,
                isUploading = state.isUploadingImage,
                onImagePicked = viewModel::onImagePicked,
                onRemoveImage = viewModel::onRemoveImage
            )

            // Box + clickable overlay + DropdownMenu: the same proven-stable pattern used in
            // AddEditScreen.kt, rather than Material3's ExposedDropdownMenuBox family (whose
            // menuAnchor()/API surface has shifted across recent Material3 releases).
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = sourceTypeLabel(state.sourceType),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.edit_quote_label_source_type)) },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showSourceTypeMenu = true }
                )
                DropdownMenu(
                    expanded = showSourceTypeMenu,
                    onDismissRequest = { showSourceTypeMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    sourceTypeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(sourceTypeLabel(type)) },
                            onClick = {
                                viewModel.onSourceTypeChanged(type)
                                showSourceTypeMenu = false
                            }
                        )
                    }
                }
            }

            AutocompleteTextField(
                value = state.sourceTitle,
                onValueChange = viewModel::onSourceTitleChanged,
                label = stringResource(R.string.edit_quote_label_source_title),
                suggestions = suggestions.sourceTitles,
                modifier = Modifier.fillMaxWidth()
            )

            AutocompleteTextField(
                value = state.speaker,
                onValueChange = viewModel::onSpeakerChanged,
                label = stringResource(R.string.edit_quote_label_speaker),
                suggestions = suggestions.speakers,
                modifier = Modifier.fillMaxWidth()
            )

            AutocompleteTextField(
                value = state.author,
                onValueChange = viewModel::onAuthorChanged,
                label = stringResource(R.string.edit_quote_label_author),
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
                label = stringResource(R.string.edit_quote_label_tags),
                suggestions = suggestions.tags,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
