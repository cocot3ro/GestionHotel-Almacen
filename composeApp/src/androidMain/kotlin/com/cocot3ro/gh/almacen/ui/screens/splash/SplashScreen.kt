package com.cocot3ro.gh.almacen.ui.screens.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cocot3ro.gh.almacen.ui.screens.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.app_image
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    modifier: Modifier,
    viewModel: SplashViewModel = koinViewModel(),
    onSetupRequired: () -> Unit,
    onSplashFinished: () -> Unit,
) {
    Box(modifier = modifier) {
        Image(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(Res.drawable.app_image),
            contentDescription = null
        )
    }

    when (val firstTimeUiState = viewModel.firstTimeUiState.collectAsState().value) {
        is UiState.Success<*> -> {

            if (firstTimeUiState.value as Boolean) {
                onSetupRequired()
            } else {
                onSplashFinished()
            }
        }

        is UiState.Error -> {
            Log.e("SplashScreen", "Error fetching prefs", firstTimeUiState.throwable)
        }

        UiState.Idle -> Unit
        UiState.Loading -> Unit
    }
}
