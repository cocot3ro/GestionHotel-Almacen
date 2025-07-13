package com.cocot3ro.gh.almacen.ui.screens.almacen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.cocot3ro.gh.almacen.BuildConfig
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.state.ItemUiState
import com.cocot3ro.gh.almacen.domain.state.ex.ForbiddenException
import com.cocot3ro.gh.almacen.domain.state.ex.NotFoundException
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import com.cocot3ro.gh.almacen.ui.activity.scanner.ScannerActivity
import com.cocot3ro.gh.almacen.ui.activity.scanner.ScannerContract
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.add_a_photo_24dp
import gh_almacen.composeapp.generated.resources.barcode_reader_24dp
import gh_almacen.composeapp.generated.resources.broken_image_24dp
import gh_almacen.composeapp.generated.resources.photo_size_select_small_24dp
import gh_almacen.composeapp.generated.resources.playlist_add_24dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.vectorResource
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystems

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditBottomSheet(
    viewModel: EditItemViewModel,
    itemState: ItemUiState,
    onEdit: (AlmacenItemDomain, Pair<ByteArray, String>?) -> Unit,
    onDismiss: () -> Unit,
    onUnauthorized: @Composable () -> Unit,
    onNotFound: suspend () -> Unit,
    onForbidden: suspend () -> Unit
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    val focusManager: FocusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest@{
            if (itemState is ItemUiState.Loading) return@onDismissRequest
            viewModel.dismiss()
            onDismiss()
        },
        sheetState = sheetState
    ) {
        val context: Context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val nameFocusRequester: FocusRequester = remember { FocusRequester() }
            val supplierFocusRequester: FocusRequester = remember { FocusRequester() }
            val stockFocusRequester: FocusRequester = remember { FocusRequester() }
            val packSizeFocusRequester: FocusRequester = remember { FocusRequester() }
            val minimumFocusRequester: FocusRequester = remember { FocusRequester() }

            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    text = "Editar",
                    fontSize = 28.sp
                )

                TextButton(
                    onClick = onClick@{
                        if (itemState is ItemUiState.Loading) return@onClick

                        coroutineScope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.dismiss()
                                    onDismiss()
                                }
                            }
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.clickable {
                    viewModel.updateShowImageSelection(true)
                }) {
                    viewModel.image?.let { image ->
                        SubcomposeAsyncImage(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(end = 8.dp),
                            model = image,
                            contentDescription = null
                        ) {
                            val state: AsyncImagePainter.State by painter.state.collectAsState()
                            when (state) {
                                AsyncImagePainter.State.Empty -> Unit
                                is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
                                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()

                                is AsyncImagePainter.State.Error -> Icon(
                                    imageVector = vectorResource(Res.drawable.broken_image_24dp),
                                    contentDescription = null
                                )
                            }
                        }

                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd),
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    } ?: run {
                        Icon(
                            modifier = Modifier
                                .size(100.dp)
                                .padding(end = 8.dp),
                            imageVector = vectorResource(Res.drawable.photo_size_select_small_24dp),
                            contentDescription = null
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(nameFocusRequester),
                    value = viewModel.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(text = "Nombre") },
                    maxLines = 2,
                    isError = viewModel.name.isBlank(),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            supplierFocusRequester.requestFocus()
                        }
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(supplierFocusRequester),
                value = viewModel.supplier.orEmpty(),
                onValueChange = viewModel::updateSupplier,
                label = { Text(text = "Proveedor") },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onNext = {
                        stockFocusRequester.requestFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRestorer(stockFocusRequester),
                    value = viewModel.quantity?.toString().orEmpty(),
                    onValueChange = viewModel::updateQuantity,
                    label = { Text(text = "Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = viewModel.quantity?.let { it < 0 } ?: true,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            packSizeFocusRequester.requestFocus()
                        }
                    )
                )

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRestorer(packSizeFocusRequester),
                    value = viewModel.packSize?.toString().orEmpty(),
                    onValueChange = viewModel::updatePackSize,
                    label = { Text(text = "Uds. pack") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = viewModel.packSize?.let { it < 0 } ?: true,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            minimumFocusRequester.requestFocus()
                        }
                    )
                )

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(minimumFocusRequester),
                    value = viewModel.minimum?.toString().orEmpty(),
                    onValueChange = viewModel::updateMinimum,
                    label = { Text(text = "Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = viewModel.minimum?.let { it < 0 } ?: false,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Códigos de barras",
                    fontSize = 24.sp
                )

                IconButton(onClick = { viewModel.toggleShowBarcodeInput() }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = if (viewModel.showBarcodeInput) Icons.Default.Close
                        else vectorResource(Res.drawable.playlist_add_24dp),
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = viewModel.showBarcodeInput) {
                val scannerLauncher: ManagedActivityResultLauncher<Intent, String?> =
                    rememberLauncherForActivityResult(
                        contract = ScannerContract(),
                        onResult = onResult@{ barcode: String? ->
                            if (barcode == null) return@onResult

                            viewModel.updateNewBarcodeInput(
                                newBarcodeInput = barcode.replace("""\D""".toRegex(), "")
                            )
                        }
                    )

                val onAppendBarcode: () -> Unit = remember {
                    onAppendBarcode@{
                        if (viewModel.newBarcodeInput.toLongOrNull() == null) return@onAppendBarcode

                        focusManager.clearFocus()

                        val result: Boolean =
                            viewModel.addBarcode(viewModel.newBarcodeInput.toLong())

                        val toastText: String = if (result) {
                            "Código de barras añadido"
                        } else {
                            "Código de barras ya existe"
                        }
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()

                        viewModel.updateNewBarcodeInput("")
                        viewModel.toggleShowBarcodeInput()
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewModel.newBarcodeInput,
                    onValueChange = {
                        viewModel.updateNewBarcodeInput(it.replace("""\D""".toRegex(), ""))
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                scannerLauncher.launch(Intent(context, ScannerActivity::class.java))
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = vectorResource(Res.drawable.barcode_reader_24dp),
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onAppendBarcode,
                            enabled = viewModel.newBarcodeInput.toLongOrNull() != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(onDone = { onAppendBarcode() })
                )
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.barcodes.takeIf(MutableSet<Long>::isNotEmpty)?.forEach { barcode ->
                    InputChip(
                        onClick = { viewModel.removeBarcode(barcode) },
                        label = { Text(barcode.toString()) },
                        selected = false,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                Modifier.size(InputChipDefaults.AvatarSize)
                            )
                        },
                    )
                } ?: run {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "No hay códigos de barras para este producto"
                    )
                }
            }

            Button(
                modifier = Modifier
                    .align(Alignment.End),
                enabled = itemState !is ItemUiState.Idle &&
                        itemState !is ItemUiState.Loading &&
                        viewModel.isValidForm(),
                onClick = {
                    focusManager.clearFocus()
                    onEdit(viewModel.getItem(), viewModel.newImageData)
                }
            ) {
                Text(
                    text = "Guardar",
                    fontSize = 20.sp
                )
            }
        }

        val cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean> =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture(),
                onResult = onResult@{ isSuccess: Boolean ->
                    if (!isSuccess) {
                        viewModel.updateNewImageTempUri(null)
                        return@onResult
                    }

                    val tempUri: Uri = viewModel.newImageTempUri!!.toUri()
                    coroutineScope.launch(Dispatchers.Default) {
                        val bytes: ByteArray = context.contentResolver
                            .openInputStream(tempUri)
                            ?.use(InputStream::readBytes)
                            ?: return@launch

                        viewModel.updateImage(
                            image = tempUri.toString(),
                            imageData = bytes to tempUri.toString()
                                .substringAfterLast(FileSystems.getDefault().separator)
                                .let {
                                    if (it.contains('.')) return@let it

                                    val extension: String = context.contentResolver.getType(tempUri)
                                        ?.substringAfterLast('/') ?: "jpg"

                                    return@let "$it.$extension"
                                }
                        )
                    }

                    viewModel.updateNewImageTempUri(null)
                }
            )

        val cameraPermission: PermissionState = rememberPermissionState(
            permission = Manifest.permission.CAMERA
        )

        if (viewModel.newImageTempUri != null) {
            if (cameraPermission.status.isGranted) {
                LaunchedEffect(viewModel.newImageTempUri) {
                    viewModel.newImageTempUri?.toUri()?.let { uri: Uri ->
                        cameraLauncher.launch(uri)
                    }
                }
            } else {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.updateNewImageTempUri(null) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            modifier = Modifier.size(100.dp),
                            imageVector = vectorResource(Res.drawable.add_a_photo_24dp),
                            contentDescription = null
                        )

                        Text(
                            text = "Permiso de cámara requerido",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(16.dp)
                        )

                        Row {
                            OutlinedButton(
                                modifier = Modifier.padding(8.dp),
                                onClick = { viewModel.updateNewImageTempUri(null) }
                            ) {
                                Text(text = "Cancelar")
                            }

                            Button(
                                modifier = Modifier.padding(8.dp),
                                onClick = {
                                    if (cameraPermission.status.shouldShowRationale) {
                                        cameraPermission.launchPermissionRequest()
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
                                Text(text = "Solicitar permiso")
                            }
                        }
                    }
                }
            }
        }

        val imagePickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia(),
                onResult = onResult@{ imageUri: Uri? ->
                    if (imageUri == null) return@onResult

                    coroutineScope.launch(Dispatchers.Default) {
                        val bytes: ByteArray = context.contentResolver
                            .openInputStream(imageUri)
                            ?.use(InputStream::readBytes)
                            ?: return@launch

                        viewModel.updateImage(
                            image = imageUri.toString(),
                            imageData = bytes to imageUri.toString()
                                .substringAfterLast(FileSystems.getDefault().separator)
                                .let {
                                    if (it.contains('.')) return@let it

                                    val extension: String =
                                        context.contentResolver.getType(imageUri)
                                            ?.substringAfterLast('/') ?: "jpg"

                                    return@let "$it.$extension"
                                }
                        )
                    }
                }
            )

        if (viewModel.showImageSelection) {
            ImageSelectionBottomSheet(
                onDismissRequest = { viewModel.updateShowImageSelection(showImageSelection = false) },
                onGallery = {
                    imagePickerLauncher.launch(
                        input = PickVisualMediaRequest(
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                    viewModel.updateShowImageSelection(showImageSelection = false)
                },
                onCamera = {
                    viewModel.updateNewImageTempUri(createImageUri(context).toString())
                    viewModel.updateShowImageSelection(showImageSelection = false)
                },
                onRemoveImage = {
                    viewModel.removeImage()
                    viewModel.updateShowImageSelection(showImageSelection = false)
                }
            )
        }
    }

    when (itemState) {
        is ItemUiState.Error -> {
            when (itemState.cause) {
                is UnauthorizedException -> {
                    focusManager.clearFocus()
                    onUnauthorized()
                }

                is NotFoundException -> LaunchedEffect(Unit) {
                    launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                focusManager.clearFocus()
                                viewModel.dismiss()
                                onDismiss()
                                runBlocking { onNotFound() }
                            }
                        }
                }

                is ForbiddenException -> LaunchedEffect(Unit) {
                    launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                focusManager.clearFocus()
                                viewModel.dismiss()
                                onDismiss()
                                runBlocking { onForbidden() }
                            }
                        }
                }
            }
        }

        ItemUiState.Success -> {
            val context: Context = LocalContext.current
            LaunchedEffect(Unit) {
                launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            focusManager.clearFocus()
                            viewModel.dismiss()
                            onDismiss()

                            Toast.makeText(
                                context,
                                "Elemento editado correctamente",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        else -> Unit
    }
}

private fun createImageUri(context: Context): Uri {
    val photoFile: File = File.createTempFile(
        "photo_${Clock.System.now()}_", ".jpg", context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        photoFile
    )
}