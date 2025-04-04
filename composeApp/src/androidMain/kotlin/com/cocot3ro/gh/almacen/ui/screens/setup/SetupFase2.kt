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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.ui.screens.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.network_manage_48dp
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SetupFase2(
    modifier: Modifier,
    viewModel: SetupFase2ViewModel = koinViewModel(),
    onSetupFaseCompleted: () -> Unit,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            when (uiState) {
                is UiState.Success<*> -> {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.completeSetup()
                            onSetupFaseCompleted()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(text = "Finalizar")
                        }
                    )
                }

                else -> {
                    ExtendedFloatingActionButton(onClick = viewModel::testConnection) {
                        Text("Conectar")
                    }
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
                text = "Configura el servidor",
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
                OutlinedTextField(
                    modifier = Modifier.weight(0.7f),
                    enabled = uiState !is UiState.Loading,
                    label = { Text("Host") },
                    prefix = { Text("https://") },
                    placeholder = { Text("192.168.0.1") },
                    singleLine = true,
                    value = viewModel.host.value,
                    onValueChange = viewModel::updateHost,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    isError = viewModel.host.status != TextFieldStatus.VALID,
                    supportingText = {
                        when (viewModel.host.status) {
                            TextFieldStatus.VALID -> Unit

                            TextFieldStatus.EMPTY_VALUE -> {
                                Text("Empty value")
                            }

                            TextFieldStatus.INVALID_FORMAT -> {
                                Text("Invalid format")
                            }

                            TextFieldStatus.INVALID_VALUE -> {
                                Text("Invalid value")
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
                    modifier = Modifier.weight(0.3f),
                    enabled = uiState !is UiState.Loading,
                    label = { Text("Port") },
                    placeholder = { Text("8080") },
                    singleLine = true,
                    value = viewModel.port.value,
                    onValueChange = viewModel::updatePort,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = viewModel.port.status != TextFieldStatus.VALID,
                    supportingText = {
                        when (viewModel.port.status) {
                            TextFieldStatus.VALID -> Unit

                            TextFieldStatus.EMPTY_VALUE -> {
                                Text("Empty value")
                            }

                            TextFieldStatus.INVALID_FORMAT -> {
                                Text("Invalid format")
                            }

                            TextFieldStatus.INVALID_VALUE -> {
                                Text("Invalid value")
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
        }
    }
}
