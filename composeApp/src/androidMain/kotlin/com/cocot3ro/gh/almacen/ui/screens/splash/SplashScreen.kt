@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.ui.state.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.app_image
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    modifier: Modifier,
    viewModel: SplashViewModel = koinViewModel(),
    onSetupRequired: () -> Unit,
    onSplashFinished: () -> Unit
) {
    Scaffold(modifier = modifier) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.app_image),
                contentDescription = null
            )

            when (val uiState: UiState = viewModel.uiState.collectAsStateWithLifecycle().value) {

                UiState.Idle -> Unit

                is UiState.Loading -> Unit
                is UiState.Reloading<*> -> Unit

                is UiState.Success<*> -> {
                    if (uiState.value as Boolean) onSetupRequired()
                    else onSplashFinished()
                }

                is UiState.Error<*> -> Unit
            }
        }
    }
}
