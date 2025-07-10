package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.LoginUiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.broken_image_24dp
import gh_almacen.composeapp.generated.resources.password
import gh_almacen.composeapp.generated.resources.visibility_24dp
import gh_almacen.composeapp.generated.resources.visibility_off_24dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun LoginDialog(
    user: UserDomain,
    state: LoginUiState,
    password: String,
    onPasswordChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onLogin: () -> Unit,
    onSuccess: () -> Unit
) {
    DisposableEffect(state) {
        onDispose {
            if (state is LoginUiState.Success) {
                onSuccess()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest@{
            if (state is LoginUiState.Loading) return@onDismissRequest
            onDismissRequest()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                user.image?.let { image ->
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 8.dp),
                        model = image,
                        contentDescription = null
                    ) {
                        val imageState: AsyncImagePainter.State by painter.state.collectAsState()
                        when (imageState) {
                            AsyncImagePainter.State.Empty -> Unit
                            is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
                            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()

                            is AsyncImagePainter.State.Error -> Icon(
                                imageVector = vectorResource(Res.drawable.broken_image_24dp),
                                contentDescription = null
                            )
                        }
                    }
                }

                Text(
                    text = user.name,
                    fontSize = 24.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp)
            ) {
                if (user.requiresPassword) {
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
                        keyboardActions = KeyboardActions(onDone = { onLogin() })
                    )
                }

                AnimatedVisibility(
                    visible = state is LoginUiState.Loading
                ) {
                    LinearProgressIndicator()
                }
            }
        },
        confirmButton = confirmButton@{
            if (!user.requiresPassword) return@confirmButton

            TextButton(onClick = onLogin) {
                Text(
                    text = "Continuar",
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = dismissButton@{
            if (!user.requiresPassword) return@dismissButton

            TextButton(onClick = onClick@{
                if (state is LoginUiState.Loading) return@onClick
                onDismissRequest()
            }) {
                Text(
                    text = "Cancelar",
                    fontSize = 16.sp
                )
            }
        }
    )
}