package com.btweeu.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A plain text field that shows a small dropdown of matching suggestions underneath
 * whenever there are any (whether or not the field currently has focus - keeping this
 * simple and permissive rather than trying to gate it on focus state, since focus
 * detection via Modifier.onFocusChanged has been unreliable for some fields in this
 * codebase before). Deliberately not built on ExposedDropdownMenuBox - that component has
 * caused problems here previously too (see AddEditScreen's category picker, which works
 * around it with a manual Box+DropdownMenu pattern).
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val filtered = if (value.isBlank()) {
        emptyList()
    } else {
        suggestions.filter { it.contains(value, ignoreCase = true) }.take(5)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            supportingText = supportingText?.let { { Text(it) } },
            interactionSource = interactionSource
        )

        if (filtered.isNotEmpty() && (isFocused || value.isNotBlank())) {
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val currentSegment = value.substringAfterLast(',').trim()
    val alreadyEntered = value.substringBeforeLast(',', missingDelimiterValue = "")
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    val filtered = if (currentSegment.isBlank()) {
        emptyList()
    } else {
        suggestions.filter {
            it.contains(currentSegment, ignoreCase = true) && it !in alreadyEntered
        }.take(5)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = supportingText?.let { { Text(it) } },
            interactionSource = interactionSource
        )

        if (filtered.isNotEmpty() && (isFocused || currentSegment.isNotBlank())) {
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
