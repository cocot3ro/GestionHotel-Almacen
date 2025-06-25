@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.screens.splash

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.domain.model.VersionInfoDomain
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.app_image
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            val uiState: SplashUiState by viewModel.uiState.collectAsStateWithLifecycle()
            when (uiState) {
                SplashUiState.Idle -> Unit
                SplashUiState.Loading -> Unit

                SplashUiState.SetupRequired -> onSetupRequired()
                is SplashUiState.UpdateRequired -> {

                    val versionInfo: VersionInfoDomain =
                        (uiState as SplashUiState.UpdateRequired).versionInfo

                    AlertDialog(
                        onDismissRequest = {},
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        ),
                        title = {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = "Actualización disponible",
                                fontSize = 24.sp
                            )
                        },
                        text = {
                            Text(text = "La versión ${versionInfo.versionName} está disponible para descargar.")
                        },
                        confirmButton = {
                            val context: Context = LocalContext.current

                            Button(onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        versionInfo.url.toUri()
                                    )
                                )
                            }) {
                                Text(text = "Actualizar")
                            }
                        }
                    )
                }

                SplashUiState.SplashUiFinished -> onSplashFinished()

                is SplashUiState.Error -> Unit
            }
        }
    }
}
