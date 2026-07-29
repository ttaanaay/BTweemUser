package com.btween.app.ui.profile

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btween.app.domain.model.SourceType
import com.btween.app.ui.components.QuoteImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSocialQuoteScreen(
    onDone: () -> Unit,
    viewModel: EditSocialQuoteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
                title = { Text("Edit quote") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::onSave, enabled = !state.isSaving && !state.isLoading) {
                        Text("Save")
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
                label = { Text("Quote text") },
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
                    value = state.sourceType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Source type") },
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
                    SourceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.onSourceTypeChanged(type)
                                showSourceTypeMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.sourceTitle,
                onValueChange = viewModel::onSourceTitleChanged,
                label = { Text("Source title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.speaker,
                onValueChange = viewModel::onSpeakerChanged,
                label = { Text("Speaker / character") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.author,
                onValueChange = viewModel::onAuthorChanged,
                label = { Text("Author (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
