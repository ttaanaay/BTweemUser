package com.btweeu.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.btweeu.app.R

/**
 * Chip-style tag input. Existing tags render as small removable chips (tap the x); typing
 * a comma, space, or hitting the keyboard's "done" action turns whatever's currently typed
 * into a new chip. [value]/[onValueChange] still use the same comma-separated String as
 * before, so this drops in wherever [TagsAutocompleteField] was used with no other changes
 * needed - it's purely a UI-layer swap.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagChipInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    val tags = remember(value) {
        value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    var draft by remember { mutableStateOf("") }

    fun commitDraft() {
        val trimmed = draft.trim()
        if (trimmed.isNotEmpty() && tags.none { it.equals(trimmed, ignoreCase = true) }) {
            onValueChange((tags + trimmed).joinToString(", "))
        }
        draft = ""
    }

    fun removeTag(tag: String) {
        onValueChange(tags.filter { it != tag }.joinToString(", "))
    }

    val filtered = if (draft.isBlank()) {
        emptyList()
    } else {
        suggestions.filter { s ->
            s.contains(draft, ignoreCase = true) && tags.none { it.equals(s, ignoreCase = true) }
        }.take(5)
    }

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))

        if (tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { removeTag(tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = draft,
            onValueChange = { new ->
                when {
                    new.endsWith(",") || new.endsWith(" ") -> {
                        draft = new.dropLast(1)
                        commitDraft()
                    }
                    else -> draft = new
                }
            },
            placeholder = { Text(stringResource(R.string.tag_input_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commitDraft() })
        )

        if (filtered.isNotEmpty()) {
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
                                onValueChange((tags + suggestion).joinToString(", "))
                                draft = ""
                            }
                        )
                    }
                }
            }
        }
    }
}
