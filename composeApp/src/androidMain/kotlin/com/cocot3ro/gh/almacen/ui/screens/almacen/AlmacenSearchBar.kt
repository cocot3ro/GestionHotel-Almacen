package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AlmacenSearchBar(
    modifier: Modifier,
    viewModel: AlmacenViewModel
) {

    OutlinedTextField(
        modifier = modifier,
        value = viewModel.filter,
        onValueChange = viewModel::updateFilter,
        label = { Text(text = "Buscar por nombre") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { viewModel.updateFilter("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null
                )
            }
        }
    )
}