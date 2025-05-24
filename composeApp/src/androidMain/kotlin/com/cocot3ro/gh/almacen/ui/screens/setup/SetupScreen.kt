package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.data.network.NetworkConstants
import com.cocot3ro.gh.almacen.domain.state.TestConnectionResult
import com.cocot3ro.gh.almacen.ui.screens.settings.TextFieldStatus
import com.cocot3ro.gh.almacen.ui.state.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.configure_server
import gh_almacen.composeapp.generated.resources.connect
import gh_almacen.composeapp.generated.resources.connection_error_explain
import gh_almacen.composeapp.generated.resources.empty_value
import gh_almacen.composeapp.generated.resources.invalid_format
import gh_almacen.composeapp.generated.resources.invalid_value
import gh_almacen.composeapp.generated.resources.ip_address
import gh_almacen.composeapp.generated.resources.ip_address_example
import gh_almacen.composeapp.generated.resources.network_manage_48dp
import gh_almacen.composeapp.generated.resources.port
import gh_almacen.composeapp.generated.resources.port_example
import gh_almacen.composeapp.generated.resources.save
import gh_almacen.composeapp.generated.resources.save_48dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SetupScreen(
    modifier: Modifier,
    viewModel: SetupViewModel = koinViewModel(),
    onSetupCompleted: () -> Unit
) {
    val uiState: UiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            when {
                uiState is UiState.Success<*> -> {
                    val coroutineScope: CoroutineScope = rememberCoroutineScope()

                    ExtendedFloatingActionButton(
                        modifier = Modifier.imePadding(),
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    viewModel.completeSetup()
                                }
                                onSetupCompleted()
                            }
                        },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = vectorResource(Res.drawable.save_48dp),
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(Res.string.save)) }
                    )
                }

                uiState !is UiState.Loading &&
                        viewModel.host.status == TextFieldStatus.VALID &&
                        viewModel.port.status == TextFieldStatus.VALID -> {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.imePadding(),
                        onClick = viewModel::testConnection,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        text = { Text(text = stringResource(Res.string.connect)) }
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f),
                imageVector = vectorResource(Res.drawable.network_manage_48dp),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(Res.string.configure_server),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                val portFocusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    modifier = Modifier.weight(0.7f),
                    enabled = uiState !is UiState.Loading,
                    label = { Text(text = stringResource(Res.string.ip_address)) },
                    prefix = { Text(text = "${NetworkConstants.SCHEME}://") },
                    placeholder = { Text(text = stringResource(Res.string.ip_address_example)) },
                    singleLine = true,
                    value = viewModel.host.value,
                    onValueChange = viewModel::updateHost,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            portFocusRequester.requestFocus()
                        }
                    ),
                    isError = viewModel.host.status != TextFieldStatus.IDLE &&
                            viewModel.host.status != TextFieldStatus.VALID,
                    supportingText = {
                        when (viewModel.host.status) {
                            TextFieldStatus.IDLE -> Unit
                            TextFieldStatus.VALID -> Unit

                            TextFieldStatus.EMPTY_VALUE -> {
                                Text(text = stringResource(Res.string.empty_value))
                            }

                            TextFieldStatus.INVALID_FORMAT -> {
                                Text(text = stringResource(Res.string.invalid_format))
                            }

                            TextFieldStatus.INVALID_VALUE -> {
                                Text(text = stringResource(Res.string.invalid_value))
                            }
                        }
                    },
                    colors = if (uiState is UiState.Success<*> && viewModel.host.status == TextFieldStatus.VALID)
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Green,
                            unfocusedBorderColor = Color.Green
                        )
                    else
                        OutlinedTextFieldDefaults.colors()
                )

                OutlinedTextField(
                    modifier = Modifier
                        .weight(0.3f)
                        .focusRequester(portFocusRequester),
                    enabled = uiState !is UiState.Loading,
                    label = { Text(text = stringResource(Res.string.port)) },
                    placeholder = { Text(text = stringResource(Res.string.port_example)) },
                    singleLine = true,
                    value = viewModel.port.value,
                    onValueChange = viewModel::updatePort,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()

                            if (uiState !is UiState.Loading &&
                                viewModel.host.status == TextFieldStatus.VALID &&
                                viewModel.port.status == TextFieldStatus.VALID
                            ) {
                                viewModel.testConnection()
                            }
                        }
                    ),
                    isError = viewModel.port.status != TextFieldStatus.IDLE &&
                            viewModel.port.status != TextFieldStatus.VALID,
                    supportingText = {
                        when (viewModel.port.status) {
                            TextFieldStatus.IDLE -> Unit
                            TextFieldStatus.VALID -> Unit

                            TextFieldStatus.EMPTY_VALUE -> {
                                Text(text = stringResource(Res.string.empty_value))
                            }

                            TextFieldStatus.INVALID_FORMAT -> {
                                Text(text = stringResource(Res.string.invalid_format))
                            }

                            TextFieldStatus.INVALID_VALUE -> {
                                Text(text = stringResource(Res.string.invalid_value))
                            }
                        }
                    },
                    colors = if (uiState is UiState.Success<*> && viewModel.port.status == TextFieldStatus.VALID)
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Green,
                            unfocusedBorderColor = Color.Green
                        )
                    else
                        OutlinedTextFieldDefaults.colors()
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                visible = uiState is UiState.Loading
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                visible = uiState is UiState.Success<*> && uiState.value is TestConnectionResult.Success
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = "Conexión exitosa", // TODO: String resource
                    color = Color.Green,
                    textAlign = TextAlign.Center
                )
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                visible = uiState is UiState.Error<*> ||
                        (uiState is UiState.Success<*> && uiState.value is TestConnectionResult.ServiceUnavailable)
            ) {
                when {
                    uiState is UiState.Error<*> -> {
                        Text(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            text = stringResource(Res.string.connection_error_explain),
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }

                    uiState is UiState.Success<*> && uiState.value is TestConnectionResult.ServiceUnavailable -> {
                        Text(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            text = "Servicio no disponible", // TODO: String resource
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
