package com.cocot3ro.gh.almacen.ui.screens.setup

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.barcode_scanner_48dp
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupFase1(
    modifier: Modifier,
    onSetupFaseCompleted: () -> Unit,
) {
    var canContinue by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (!canContinue) return@Scaffold

            ExtendedFloatingActionButton(
                onClick = onSetupFaseCompleted,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = null
                    )
                },
                text = {
                    Text(text = "Continuar sin permiso")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var iWasRationale by remember { mutableStateOf(false) }
            var permanentDenied by remember { mutableStateOf(false) }

            val permissionState =
                rememberPermissionState(android.Manifest.permission.CAMERA) { isGranted ->
                    if (isGranted) return@rememberPermissionState

                    if (iWasRationale) {
                        permanentDenied = true
                    }
                }

            Icon(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.5f)
                    .aspectRatio(1f),
                imageVector = vectorResource(Res.drawable.barcode_scanner_48dp),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(20.dp))

            var displayText by remember { mutableStateOf("Se necesita acceso a la cámara para escanear códigos de barras.") }

            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = displayText,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            var buttonText by remember { mutableStateOf("Solicitar permiso") }

            var action: () -> Unit by remember { mutableStateOf(permissionState::launchPermissionRequest) }

            Button(onClick = action) {
                Text(text = buttonText)
            }

            when {
                permissionState.status.isGranted -> {
                    LaunchedEffect(Unit) {
                        onSetupFaseCompleted()
                    }
                }

                permissionState.status.shouldShowRationale -> {
                    displayText =
                        "La cámara es necesaria para escanear códigos de barras, concede el permiso para usar esta característica"
                    iWasRationale = true
                    canContinue = true

                }

                permanentDenied -> {
                    canContinue = true

                    val context = LocalContext.current
                    action = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = ("package:" + context.packageName).toUri()
                        }
                        context.startActivity(intent)
                    }

                    buttonText = "Permiso denegado"
                }
            }
        }
    }
}
