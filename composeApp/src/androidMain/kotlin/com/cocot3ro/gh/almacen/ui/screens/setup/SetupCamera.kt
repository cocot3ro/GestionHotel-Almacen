package com.cocot3ro.gh.almacen.ui.screens.setup

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.barcode_scanner_48dp
import gh_almacen.composeapp.generated.resources.camera_permission_message
import gh_almacen.composeapp.generated.resources.camera_permission_rationale
import gh_almacen.composeapp.generated.resources.continue_without_permission
import gh_almacen.composeapp.generated.resources.permission_denied
import gh_almacen.composeapp.generated.resources.request_permission
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupCamera(
    modifier: Modifier,
    viewModel: SetupCameraViewModel = koinViewModel<SetupCameraViewModel>(),
    onSetupCompleted: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (!viewModel.canContinue) return@Scaffold

            ExtendedFloatingActionButton(
                modifier = Modifier.padding(
                    paddingValues = WindowInsets.navigationBars
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                ),
                onClick = onSetupCompleted,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = null
                    )
                },
                text = {
                    Text(text = stringResource(Res.string.continue_without_permission))
                }
            )
        }
    ) { innerPadding ->

        val orientation: Int = LocalConfiguration.current.orientation
        val context: Context = LocalContext.current
        val permissionState: PermissionState =
            rememberPermissionState(android.Manifest.permission.CAMERA) { isGranted ->
                if (isGranted) {
                    onSetupCompleted()
                    return@rememberPermissionState
                }

                if (viewModel.iWasRationale) {
                    viewModel.updatePermanentDenied(true)
                }
            }

        val displayText: StringResource =
            if (permissionState.status.shouldShowRationale) Res.string.camera_permission_rationale
            else Res.string.camera_permission_message

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Arrangement.Top
                } else {
                    Arrangement.Center
                }
            ) {
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Icon(
                    modifier = Modifier
                        .padding(vertical = 0.dp)
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f),
                    imageVector = vectorResource(Res.drawable.barcode_scanner_48dp),
                    contentDescription = null
                )

                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Text(
                        modifier = Modifier.padding(top = 16.dp, start = 20.dp, end = 20.dp),
                        text = stringResource(displayText),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                if (viewModel.permanentDenied) {
                                    val intent: Intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .apply {
                                                data = "package:${context.packageName}".toUri()
                                            }
                                    context.startActivity(intent)
                                } else {
                                    permissionState.launchPermissionRequest()
                                }
                            }
                        ) {
                            val text: StringResource =
                                if (viewModel.permanentDenied) Res.string.permission_denied
                                else Res.string.request_permission

                            Text(text = stringResource(text))
                        }
                    }
                }
            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = stringResource(displayText),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (viewModel.permanentDenied) {
                                val intent: Intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .apply { data = "package:${context.packageName}".toUri() }
                                context.startActivity(intent)
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        val text: StringResource =
                            if (viewModel.permanentDenied) Res.string.permission_denied
                            else Res.string.request_permission

                        Text(text = stringResource(text))
                    }
                }
            }
        }

        when {
            permissionState.status.isGranted -> {
                onSetupCompleted()
            }

            permissionState.status.shouldShowRationale -> {
                viewModel.updateIWasRationale(true)
                viewModel.updateCanContinue(true)
            }

            viewModel.permanentDenied -> {
                viewModel.updateCanContinue(true)
            }
        }
    }
}
