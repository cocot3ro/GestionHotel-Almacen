package com.cocot3ro.gh.almacen.ui.activity.scanner

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.cocot3ro.gh.almacen.ui.theme.GhAlmacenTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.barcode_scanner_48dp
import gh_almacen.composeapp.generated.resources.camera_permission_message
import gh_almacen.composeapp.generated.resources.camera_permission_rationale
import gh_almacen.composeapp.generated.resources.permission_denied
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import java.util.concurrent.TimeUnit

class ScannerActivity : ComponentActivity() {

    private val viewModel: ScannerViewModel by viewModels<ScannerViewModel>()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val permissionState: PermissionState =
                rememberPermissionState(android.Manifest.permission.CAMERA)

            if (permissionState.status.isGranted) {
                GhAlmacenTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        ScannerScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { barcode: String ->

                            val resultIntent: Intent = Intent().apply {
                                putExtra("scanner_result", barcode)
                            }

                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    }
                }
            } else {
                RequestPermissionScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Composable
    private fun ScannerScreen(
        modifier: Modifier,
        onBarcodeScanned: (String) -> Unit
    ) {
        val context: Context = LocalContext.current
        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
        val previewView: PreviewView = remember { PreviewView(context) }
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = remember {
            ProcessCameraProvider.getInstance(context)
        }

        AndroidView(
            factory = { previewView },
            modifier = modifier
        ) { view ->
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview: Preview = Preview.Builder().build().also { preview ->
                    preview.surfaceProvider = view.surfaceProvider
                }

                val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()
                val imageAnalysis: ImageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            viewModel.scanBarcode(
                                context = context,
                                imageProxy = imageProxy,
                                scanner = barcodeScanner,
                                onBarcodeScanned = onBarcodeScanned
                            )
                        }
                    }

                val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                val camera: Camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // 🚀 Forzar autofocus al centro al iniciar
                val cameraControl: CameraControl = camera.cameraControl
                val factory: MeteringPointFactory = previewView.meteringPointFactory
                val point: MeteringPoint = factory.createPoint(view.width / 2f, view.height / 2f)
                val action: FocusMeteringAction = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                cameraControl.startFocusAndMetering(action)

                // 🚀 Opcional: permitir reenfocar al tocar la pantalla
                previewView.setOnTouchListener { view, event ->
                    view.performClick()

                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val tapPoint: MeteringPoint = factory.createPoint(event.x, event.y)
                        val tapAction: FocusMeteringAction = FocusMeteringAction.Builder(tapPoint, FocusMeteringAction.FLAG_AF)
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)
                            .build()
                        cameraControl.startFocusAndMetering(tapAction)
                    }

                    true
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun RequestPermissionScreen(modifier: Modifier) {
        Scaffold(modifier = modifier) { innerPadding ->

            val context: Context = LocalContext.current
            val orientation: Int = LocalConfiguration.current.orientation
            val permissionState: PermissionState =
                rememberPermissionState(android.Manifest.permission.CAMERA)

            val displayText: StringResource = if (permissionState.status.shouldShowRationale) {
                Res.string.camera_permission_rationale
            } else {
                Res.string.camera_permission_message
            }

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
                                    if (permissionState.status.shouldShowRationale) {
                                        permissionState.launchPermissionRequest()
                                    } else {
                                        val intent: Intent =
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                .apply {
                                                    data = "package:${context.packageName}".toUri()
                                                }
                                        context.startActivity(intent)
                                    }
                                }
                            ) {
                                val text: StringResource =
                                    if (permissionState.status.shouldShowRationale) Res.string.camera_permission_rationale
                                    else Res.string.permission_denied

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
                                if (permissionState.status.shouldShowRationale) {
                                    permissionState.launchPermissionRequest()
                                } else {
                                    val intent: Intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .apply {
                                                data = "package:${context.packageName}".toUri()
                                            }
                                    context.startActivity(intent)
                                }
                            }
                        ) {
                            val text: StringResource =
                                if (permissionState.status.shouldShowRationale) Res.string.camera_permission_rationale
                                else Res.string.permission_denied

                            Text(text = stringResource(text))
                        }
                    }
                }
            }
        }
    }
}
