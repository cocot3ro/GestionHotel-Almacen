@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.screens.cargadescarga

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.CargaDescargaMode
import com.cocot3ro.gh.almacen.domain.state.UiState
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import com.cocot3ro.gh.almacen.ui.activity.scanner.ScannerActivity
import com.cocot3ro.gh.almacen.ui.activity.scanner.ScannerContract
import com.cocot3ro.gh.almacen.ui.dialogs.UnauthorizedDialog
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.barcode_reader_24dp
import gh_almacen.composeapp.generated.resources.delivery_truck_speed_24dp
import gh_almacen.composeapp.generated.resources.remove_24dp
import gh_almacen.composeapp.generated.resources.trolley_24dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CargaDescargaScreen(
    modifier: Modifier,
    viewModel: CargaDescargaViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val context: Context = LocalContext.current

    val scannerLauncher: ManagedActivityResultLauncher<Intent, String?> =
        rememberLauncherForActivityResult(
            contract = ScannerContract(),
            onResult = onResult@{ barcode: String? ->
                if (barcode == null) return@onResult

                val result: Boolean = viewModel.selectByBarcode(barcode)
                if (!result) {
                    Toast.makeText(context, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        )

    val itemsUiState: UiState = viewModel.itemsUiState.collectAsStateWithLifecycle().value
    val cargaUiState: UiState = viewModel.cargaUiState.collectAsStateWithLifecycle().value

    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = viewModel.loggedUser.name,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = actions@{
                    if (itemsUiState !is UiState.Success<*>) return@actions

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
                }
            )
        },
        floatingActionButton = floatingActionButton@{
            if (viewModel.cargaMap.isEmpty()) return@floatingActionButton

            val navBarsPadding: PaddingValues = WindowInsets.navigationBars
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues()

            FloatingActionButton(
                modifier = Modifier.padding(navBarsPadding),
                onClick = viewModel::performStockUpdate
            ) {
                Icon(
                    imageVector = when (viewModel.cargaDescargaMode) {
                        CargaDescargaMode.CARGA -> vectorResource(Res.drawable.delivery_truck_speed_24dp)
                        CargaDescargaMode.DESCARGA -> vectorResource(Res.drawable.trolley_24dp)
                    },
                    contentDescription = null
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                visible = itemsUiState is UiState.Success<*>
            ) {
                DockedSearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = viewModel.searchText,
                            onQueryChange = viewModel::updateFilter,
                            onSearch = { viewModel.updateShowSearch(false) },
                            expanded = viewModel.showSearch,
                            onExpandedChange = viewModel::updateShowSearch,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = trailingIcon@{
                                if (!viewModel.showSearch) return@trailingIcon
                                IconButton(onClick = { viewModel.updateShowSearch(false) }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    },
                    expanded = viewModel.showSearch,
                    onExpandedChange = viewModel::updateShowSearch,
                ) {
                    if (itemsUiState !is UiState.Success<*>) return@DockedSearchBar

                    @Suppress("UNCHECKED_CAST")
                    val items: List<AlmacenItemDomain> =
                        (itemsUiState as UiState.Success<List<AlmacenItemDomain>>).value

                    LazyColumn {
                        items(items) { item: AlmacenItemDomain ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateShowSearch(false)
                                        viewModel.selectItem(item)
                                    },
                                leadingContent = {
                                    Box(modifier = Modifier.size(48.dp)) {
                                        item.image?.let { image: String ->
                                            AsyncImage(
                                                modifier = Modifier.fillMaxSize(),
                                                model = image,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                },
                                headlineContent = {
                                    Text(
                                        text = item.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = "Stock: ${item.quantity}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = cargaUiState is UiState.Loading) {
                LinearProgressIndicator()
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(220.dp),
                contentPadding = PaddingValues(all = 8.dp)
            ) {
                if (viewModel.cargaMap.isEmpty()) {
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = "No hay elementos seleccionados"
                            )
                        }
                    }
                } else {
                    items(viewModel.cargaMap.toList()) { (item: AlmacenItemDomain, quantity: Int?) ->
                        val swipeToDismissState: SwipeToDismissBoxState =
                            rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissBoxValue: SwipeToDismissBoxValue ->
                                    when (dismissBoxValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            viewModel.selectItem(item)
                                        }

                                        SwipeToDismissBoxValue.EndToStart -> {
                                            viewModel.removeItem(item)
                                        }

                                        SwipeToDismissBoxValue.Settled -> Unit
                                    }

                                    dismissBoxValue != SwipeToDismissBoxValue.StartToEnd
                                }
                            )

                        SwipeToDismissBox(
                            modifier = Modifier.fillMaxWidth(),
                            state = swipeToDismissState,
                            backgroundContent = {
                                when (swipeToDismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        Icon(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Blue)
                                                .wrapContentSize(Alignment.CenterStart)
                                                .padding(12.dp),
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }

                                    SwipeToDismissBoxValue.EndToStart -> {
                                        Icon(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Red)
                                                .wrapContentSize(Alignment.CenterEnd)
                                                .padding(12.dp),
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove item",
                                            tint = Color.White
                                        )
                                    }

                                    SwipeToDismissBoxValue.Settled -> Unit
                                }
                            },
                            content = {
                                ListItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingContent = {
                                        Box(modifier = Modifier.size(48.dp)) {
                                            item.image?.let { image: String ->
                                                AsyncImage(
                                                    modifier = Modifier.fillMaxSize(),
                                                    model = image,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    },
                                    headlineContent = {
                                        Text(
                                            text = item.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textDecoration = TextDecoration.LineThrough.takeIf { quantity == null }
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = "Stock: ${item.quantity}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textDecoration = TextDecoration.LineThrough.takeIf { quantity == null }
                                        )
                                    },
                                    trailingContent = {
                                        if (quantity != null) {
                                            Text(
                                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                                text = "+$quantity"
                                            )
                                        } else {
                                            val coroutineScope: CoroutineScope =
                                                rememberCoroutineScope()

                                            IconButton(onClick = {
                                                coroutineScope.launch {
                                                    snackBarHostState.showSnackbar(
                                                        message = "Este elemento ha sido eliminado del servidor",
                                                        withDismissAction = true,
                                                        duration = SnackbarDuration.Long
                                                    )

                                                    viewModel.removeItem(item)
                                                }
                                            }) {
                                                Icon(
                                                    modifier = Modifier.size(24.dp),
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Item deleted"
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }

            viewModel.newItem?.let { (item: AlmacenItemDomain, amount: Int?, min: Int, max: Int) ->
                val sheetState: SheetState = rememberModalBottomSheetState()

                val coroutineScope: CoroutineScope = rememberCoroutineScope()

                ModalBottomSheet(onDismissRequest = viewModel::clearSelectItem) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = item.name,
                                fontSize = 28.sp
                            )

                            TextButton(
                                onClick = onClick@{
                                    coroutineScope.launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                viewModel.clearSelectItem()
                                            }
                                        }
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }

                        Text(
                            text = "Stock actual: ${item.quantity}",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                enabled = amount != null && amount > min,
                                onClick = viewModel::decrementAmount
                            ) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.remove_24dp),
                                    contentDescription = null
                                )
                            }

                            OutlinedTextField(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                label = { Text(text = "Cantidad") },
                                value = amount?.toString().orEmpty(),
                                onValueChange = viewModel::updateAmount,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            FilledTonalIconButton(
                                enabled = amount != null && amount < max,
                                onClick = viewModel::incrementAmount
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            modifier = Modifier.align(Alignment.End),
                            enabled = amount in min..max,
                            onClick = viewModel::addItem
                        ) {
                            Text(
                                text = "Continuar",
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }

        when (cargaUiState) {
            UiState.Idle,
            is UiState.Reloading<*> -> Unit

            UiState.Loading -> Unit

            is UiState.Success<*> -> {
                Toast.makeText(context, "Stock modificado", Toast.LENGTH_SHORT).show()

                viewModel.dismiss()
                onNavigateBack()
            }

            is UiState.Error<*> -> {
                when (cargaUiState.cause) {
                    is UnauthorizedException -> {
                        UnauthorizedDialog(onAccept = {
                            viewModel.dismiss()
                            onUnauthorized()
                        })
                    }
                }
            }
        }
    }
}
