package com.cocot3ro.gh.almacen.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onSettingsChanged: () -> Unit,
) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
//        floatingActionButton = {
//            when {
//                uiState is UiState.Success<*> -> {
//                    val coroutineScope = rememberCoroutineScope()
//
//                    ExtendedFloatingActionButton(
//                        onClick = {
//                            coroutineScope.launch {
//                                withContext(Dispatchers.IO) {
//                                    viewModel.completeSetup()
//                                }
//                                onSettingsChanged()
//                            }
//                        },
//                        icon = {
//                            Icon(
//                                imageVector = vectorResource(Res.drawable.save_48dp),
//                                contentDescription = null
//                            )
//                        },
//                        text = {
//                            Text(text = stringResource(Res.string.save))
//                        }
//                    )
//                }
//
//                uiState !is UiState.Loading &&
//                        viewModel.host.status == TextFieldStatus.VALID &&
//                        viewModel.port.status == TextFieldStatus.VALID -> {
//                    ExtendedFloatingActionButton(
//                        onClick = viewModel::testConnection,
//                        icon = {
//                            Icon(
//                                imageVector = Icons.Default.Search,
//                                contentDescription = null
//                            )
//                        },
//                        text = {
//                            Text(text = stringResource(Res.string.connect))
//                        }
//                    )
//                }
//
//                else -> Unit
//            }
//        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Settings")

//            Icon(
//                modifier = Modifier
//                    .padding(top = 16.dp)
//                    .fillMaxWidth(0.4f)
//                    .aspectRatio(1f),
//                imageVector = vectorResource(Res.drawable.network_manage_48dp),
//                contentDescription = null
//            )
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            Text(
//                modifier = Modifier.padding(horizontal = 24.dp),
//                text = stringResource(Res.string.configure_server),
//                textAlign = TextAlign.Center,
//                fontSize = 18.sp
//            )
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 24.dp),
//                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
//            ) {
//                OutlinedTextField(
//                    modifier = Modifier.weight(0.7f),
//                    enabled = uiState !is UiState.Loading,
//                    label = { Text(text = stringResource(Res.string.ip_address)) },
//                    prefix = { Text(text = stringResource(Res.string.http_prefix)) },
//                    placeholder = { Text(text = stringResource(Res.string.ip_address_example)) },
//                    singleLine = true,
//                    value = viewModel.host.value,
//                    onValueChange = viewModel::updateHost,
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
//                    isError = viewModel.host.status != TextFieldStatus.IDLE &&
//                            viewModel.host.status != TextFieldStatus.VALID,
//                    supportingText = {
//                        when (viewModel.host.status) {
//                            TextFieldStatus.IDLE -> Unit
//                            TextFieldStatus.VALID -> Unit
//
//                            TextFieldStatus.EMPTY_VALUE -> {
//                                Text(text = stringResource(Res.string.empty_value))
//                            }
//
//                            TextFieldStatus.INVALID_FORMAT -> {
//                                Text(text = stringResource(Res.string.invalid_format))
//                            }
//
//                            TextFieldStatus.INVALID_VALUE -> {
//                                Text(text = stringResource(Res.string.invalid_value))
//                            }
//                        }
//                    },
//                    colors = if (uiState is UiState.Success<*> && viewModel.host.status == TextFieldStatus.VALID)
//                        OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color.Green,
//                            unfocusedBorderColor = Color.Green
//                        )
//                    else
//                        OutlinedTextFieldDefaults.colors()
//                )
//
//                OutlinedTextField(
//                    modifier = Modifier.weight(0.3f),
//                    enabled = uiState !is UiState.Loading,
//                    label = { Text(text = stringResource(Res.string.port)) },
//                    placeholder = { Text(text = stringResource(Res.string.port_example)) },
//                    singleLine = true,
//                    value = viewModel.port.value,
//                    onValueChange = viewModel::updatePort,
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = viewModel.port.status != TextFieldStatus.IDLE &&
//                            viewModel.port.status != TextFieldStatus.VALID,
//                    supportingText = {
//                        when (viewModel.port.status) {
//                            TextFieldStatus.IDLE -> Unit
//                            TextFieldStatus.VALID -> Unit
//
//                            TextFieldStatus.EMPTY_VALUE -> {
//                                Text(text = stringResource(Res.string.empty_value))
//                            }
//
//                            TextFieldStatus.INVALID_FORMAT -> {
//                                Text(text = stringResource(Res.string.invalid_format))
//                            }
//
//                            TextFieldStatus.INVALID_VALUE -> {
//                                Text(text = stringResource(Res.string.invalid_value))
//                            }
//                        }
//                    },
//                    colors = if (uiState is UiState.Success<*> && viewModel.port.status == TextFieldStatus.VALID)
//                        OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color.Green,
//                            unfocusedBorderColor = Color.Green
//                        )
//                    else
//                        OutlinedTextFieldDefaults.colors()
//                )
//            }
//
//            AnimatedVisibility(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 24.dp),
//                visible = uiState is UiState.Loading
//            ) {
//                LinearProgressIndicator(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 24.dp)
//                )
//            }
//
//            AnimatedVisibility(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 24.dp),
//                visible = uiState is UiState.Error<*>
//            ) {
//                Text(
//                    modifier = Modifier.padding(horizontal = 24.dp),
//                    text = stringResource(Res.string.connection_error_explain),
//                    color = Color.Red,
//                    textAlign = TextAlign.Center
//                )
//            }
        }
    }
}
