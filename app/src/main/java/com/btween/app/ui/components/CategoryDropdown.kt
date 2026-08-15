package com.btween.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.btween.app.R
import com.btween.app.domain.repository.PublicCategory
import com.btween.app.ui.util.categoryIconFor

@Composable
fun CategoryDropdown(
    options: List<PublicCategory>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = options.firstOrNull { it.name == selected }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCategory?.name ?: stringResource(R.string.add_edit_category_none),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.add_edit_label_category)) },
            leadingIcon = {
                Icon(imageVector = categoryIconFor(selectedCategory?.icon), contentDescription = null)
            },
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
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_edit_category_none)) },
                onClick = { onSelected(null); expanded = false }
            )
            options.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    leadingIcon = { Icon(imageVector = categoryIconFor(category.icon), contentDescription = null) },
                    onClick = {
                        onSelected(category.name)
                        expanded = false
                    }
                )
            }
        }
    }
}
