package com.cocot3ro.gh.almacen.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.data.network.NetworkConstants
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.ui.state.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.network_manage_48dp
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState: Pair<UiState, UiState> by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = fab@{
            Column(
                modifier = Modifier.padding(
                    paddingValues = WindowInsets.navigationBars
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                ),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(onClick = onNavigateToSettings) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = vectorResource(Res.drawable.network_manage_48dp),
                        contentDescription = null
                    )
                }

                if (uiState.first !is UiState.Success<*> || uiState.second !is UiState.Error<*>)
                    return@fab

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = viewModel::retry,
                    icon = {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "Reintentar",
                            fontSize = 16.sp
                        )
                    }
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                when {
                    uiState.first is UiState.Loading || uiState.second is UiState.Loading -> {
                        when {
                            uiState.first is UiState.Loading -> {
                                Text(text = "")
                                Text(text = "Cargando configuración ...")
                            }

                            else -> {
                                val connectionValues = uiState.first as UiState.Success<*>
                                val p: Pair<*, *> = connectionValues.value as Pair<*, *>
                                val host: String = p.first as String
                                val port: UShort = p.second as UShort

                                Text(text = "Conectando con el servidor ...")
                                Text(text = "${NetworkConstants.SCHEME}://$host:$port")
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator()
                    }

                    uiState.first is UiState.Success<*> &&
                            ((uiState.second as? UiState.Success<*>)?.value is ResponseState.OK<*>) -> {
                        onNavigateToLogin()
                    }

                    uiState.second is UiState.Error<*> -> {
                        Text(
                            text = "Error al conectar con el servidor",
                            color = Color.Red,
                        )
                    }
                }
            }
        }
    }
}
