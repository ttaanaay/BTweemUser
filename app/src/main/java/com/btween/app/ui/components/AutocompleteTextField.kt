package com.btween.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

/**
 * A plain text field that shows a small dropdown of matching suggestions underneath while
 * focused and non-empty. Deliberately not built on ExposedDropdownMenuBox - that component
 * has been unreliable in this codebase before (see AddEditScreen's category picker, which
 * works around the same issue with a manual Box+DropdownMenu pattern). A field + a plain
 * Surface/LazyColumn below it behaves predictably instead.
 */
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    supportingText: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val filtered = remember(value, suggestions) {
        if (value.isBlank()) {
            emptyList()
        } else {
            suggestions.filter { it.contains(value, ignoreCase = true) && !it.equals(value, ignoreCase = true) }
                .take(5)
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = singleLine,
            supportingText = supportingText?.let { { Text(it) } }
        )

        if (isFocused && filtered.isNotEmpty()) {
            Surface(
                tonalElevation = 3.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(filtered) { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = { onValueChange(suggestion) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Same idea as [AutocompleteTextField], but for a comma-separated tags field - matches and
 * completes only the segment currently being typed (after the last comma), so picking a
 * suggestion doesn't wipe out tags already entered earlier in the field.
 */
@Composable
fun TagsAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val currentSegment = value.substringAfterLast(',').trim()
    val alreadyEntered = value.substringBeforeLast(',', missingDelimiterValue = "")
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    val filtered = remember(currentSegment, suggestions, alreadyEntered) {
        if (currentSegment.isBlank()) {
            emptyList()
        } else {
            suggestions.filter {
                it.contains(currentSegment, ignoreCase = true) &&
                    !it.equals(currentSegment, ignoreCase = true) &&
                    it !in alreadyEntered
            }.take(5)
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            supportingText = supportingText?.let { { Text(it) } }
        )

        if (isFocused && filtered.isNotEmpty()) {
            Surface(
                tonalElevation = 3.dp,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(filtered) { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                val prefix = value.substringBeforeLast(',', missingDelimiterValue = "")
                                val newValue = if (prefix.isBlank()) suggestion else "$prefix, $suggestion"
                                onValueChange("$newValue, ")
                            }
                        )
                    }
                }
            }
        }
    }
}
