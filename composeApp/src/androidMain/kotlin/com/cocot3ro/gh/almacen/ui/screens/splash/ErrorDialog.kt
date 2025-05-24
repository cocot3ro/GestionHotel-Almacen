package com.cocot3ro.gh.almacen.ui.screens.splash

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties


@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
        title = {
            Text(text = "Error inesperado")
        },
        text = {
            Text("Se ha producido un error inesperado. Por favor, reinicie la aplicación.")
        },
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cerrar")
            }
        }
    )
}