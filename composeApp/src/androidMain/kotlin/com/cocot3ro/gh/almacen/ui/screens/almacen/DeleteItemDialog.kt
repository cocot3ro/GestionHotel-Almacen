package com.cocot3ro.gh.almacen.ui.screens.almacen

import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.state.ItemUiState
import com.cocot3ro.gh.almacen.domain.state.ex.ForbiddenException
import com.cocot3ro.gh.almacen.domain.state.ex.NotFoundException
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException

@Composable
fun DeleteItemDialog(
    item: AlmacenItemDomain,
    itemState: ItemUiState,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onUnauthorized: @Composable () -> Unit,
    onNotFound: suspend () -> Unit,
    onForbidden: suspend () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null
            )
        },
        title = {
            Text(text = "Seguro que quieres borrar ${item.name}?")
        },
        text = {
            if (itemState is ItemUiState.Loading) {
                LinearProgressIndicator()
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(text = "Cancelar")
            }
        },
        confirmButton = {
            Button(onClick = onDelete) {
                Text(text = "Borrar")
            }
        }
    )

    when (itemState) {
        ItemUiState.Idle -> Unit
        ItemUiState.Waiting -> Unit
        ItemUiState.Loading -> Unit

        ItemUiState.Success -> {
            val context: Context = LocalContext.current
            onDismissRequest()
            Toast.makeText(context, "Item ${item.name} borrado", Toast.LENGTH_LONG).show()
        }

        is ItemUiState.Error -> {
            when (itemState.cause) {
                is UnauthorizedException -> {
                    onUnauthorized()
                }

                is NotFoundException -> LaunchedEffect(Unit) {
                    onDismissRequest()
                    onNotFound()
                }

                is ForbiddenException -> LaunchedEffect(Unit) {
                    onDismissRequest()
                    onForbidden()
                }
            }
        }
    }
}