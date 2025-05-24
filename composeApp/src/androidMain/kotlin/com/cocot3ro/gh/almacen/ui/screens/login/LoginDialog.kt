package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.password
import gh_almacen.composeapp.generated.resources.visibility_24dp
import gh_almacen.composeapp.generated.resources.visibility_off_24dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun LoginDialog(
    user: AlmacenUserDomain,
    state: LoginUiState,
    password: String,
    onPasswordChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onLogin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                user.image?.let { image ->
                    AsyncImage(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp),
                        model = image,
                        contentDescription = null,
                    )
                }

                Text(
                    text = user.name,
                    fontSize = 24.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            ) {
                var isPasswordVisible: Boolean by remember { mutableStateOf(false) }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = onPasswordChange,
                    enabled = state !is LoginUiState.Loading,
                    isError = state is LoginUiState.Fail,
                    label = {
                        Text(text = stringResource(Res.string.password))
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) {
                                    vectorResource(Res.drawable.visibility_24dp)
                                } else {
                                    vectorResource(Res.drawable.visibility_off_24dp)
                                },
                                contentDescription = null
                            )
                        }
                    },
                    keyboardActions = KeyboardActions(
                        onDone = { onLogin() }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onLogin) {
                Text(
                    text = "Continuar",
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "Cancelar",
                    fontSize = 16.sp
                )
            }
        }
    )
}