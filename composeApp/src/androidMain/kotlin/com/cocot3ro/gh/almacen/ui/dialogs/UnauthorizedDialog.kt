package com.cocot3ro.gh.almacen.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.lock_person_24dp
import org.jetbrains.compose.resources.vectorResource

@Composable
fun UnauthorizedDialog(
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        icon = {
            Icon(
                imageVector = vectorResource(Res.drawable.lock_person_24dp),
                contentDescription = null
            )
        },
        title = {
            Text(text = "Sesión caducada")
        },
        text = {
            Text(text = "Vuelve a iniciar sesión")
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text(text = "Aceptar")
            }
        }
    )
}