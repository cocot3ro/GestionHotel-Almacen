@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.screens.almacen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain
import com.cocot3ro.gh.almacen.ui.dialogs.UnauthorizedDialog
import com.cocot3ro.gh.almacen.ui.state.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.abc_24dp
import gh_almacen.composeapp.generated.resources.barcode_24dp
import gh_almacen.composeapp.generated.resources.delivery_truck_speed_24dp
import gh_almacen.composeapp.generated.resources.filter_alt_24dp
import gh_almacen.composeapp.generated.resources.format_list_bulleted_24dp
import gh_almacen.composeapp.generated.resources.format_list_numbered_24dp
import gh_almacen.composeapp.generated.resources.format_list_numbered_rtl_24dp
import gh_almacen.composeapp.generated.resources.search_off_24dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlmacenScreen(
    modifier: Modifier,
    viewModel: AlmacenViewModel = koinViewModel<AlmacenViewModel>(),
    onNavigateBack: () -> Unit
) {
    val uiState: UiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val snackbarHost: SnackbarHostState = remember { SnackbarHostState() }

    val lazyGridState: LazyGridState = rememberLazyGridState()

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = viewModel.loggedUser.name,
                            overflow = TextOverflow.Ellipsis
                        )

                        val orientation: Int = LocalConfiguration.current.orientation

                        AnimatedVisibility(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(horizontal = 8.dp),
                            visible = orientation == Configuration.ORIENTATION_LANDSCAPE &&
                                    viewModel.showSearch
                        ) {
                            SearchBar(
                                modifier = Modifier.fillMaxWidth(),
                                viewModel = viewModel
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.updateShowSearch(viewModel.showSearch.not()) },
                        enabled = uiState is UiState.Success<*>
                    ) {
                        Icon(
                            imageVector = if (viewModel.showSearch) {
                                vectorResource(Res.drawable.search_off_24dp)
                            } else {
                                Icons.Default.Search
                            },
                            contentDescription = null
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { viewModel.updateShowSortMode(viewModel.showSortMode.not()) },
                            enabled = uiState is UiState.Success<*>
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.format_list_bulleted_24dp),
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = viewModel.showSortMode,
                            onDismissRequest = { viewModel.updateShowSortMode(showSortMode = false) }
                        ) {
                            SortMode.entries.forEach { sortMode ->
                                val label: String = when (sortMode) {
                                    SortMode.ID -> "ID"
                                    SortMode.NAME -> "Nombre"
                                    SortMode.QUANTITY -> "Cantidad"
                                }

                                val icon: DrawableResource = when (sortMode) {
                                    SortMode.ID -> Res.drawable.format_list_numbered_24dp
                                    SortMode.NAME -> Res.drawable.abc_24dp
                                    SortMode.QUANTITY -> Res.drawable.format_list_numbered_rtl_24dp
                                }

                                DropdownMenuItem(
                                    text = { Text(text = label) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = vectorResource(icon),
                                            contentDescription = null
                                        )
                                    },
                                    trailingIcon = trailingIcon@{
                                        if (viewModel.sortMode != sortMode) return@trailingIcon

                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateSortMode(sortMode)
                                        lazyGridState.requestScrollToItem(index = 0)
                                        viewModel.updateShowSortMode(false)
                                    }
                                )
                            }

                            HorizontalDivider()

                            FilterChip(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                selected = viewModel.showLowStockFirst,
                                onClick = {
                                    viewModel.updateShowLowStockFirst(viewModel.showLowStockFirst.not())
                                    lazyGridState.requestScrollToItem(index = 0)
                                    viewModel.updateShowSortMode(false)
                                },
                                label = { Text(text = "Mostrar primero bajo stock") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = floatingActionButton@{
            if (viewModel.loggedUser.role != UserRoleDomain.ADMIN) return@floatingActionButton

            val navBarsPadding: PaddingValues = WindowInsets.navigationBars
                .only(WindowInsetsSides.Horizontal)
                .asPaddingValues()

            FloatingActionButton(
                modifier = Modifier.padding(navBarsPadding),
                onClick = viewModel::setCreate
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val orientation: Int = LocalConfiguration.current.orientation

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                visible = orientation == Configuration.ORIENTATION_PORTRAIT && viewModel.showSearch
            ) {
                SearchBar(
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = viewModel
                )
            }

            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isRefreshing = uiState is UiState.Reloading<*>,
                onRefresh = viewModel::refresh
            ) {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyGridState,
                    columns = GridCells.Adaptive(minSize = 350.dp),
                    contentPadding = PaddingValues(all = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (uiState) {
                        is UiState.Idle -> Unit

                        is UiState.Loading -> {
                            items(count = 50) { _ ->
                                ItemShimmer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(205.dp)
                                )
                            }
                        }

                        is UiState.Reloading<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val items = uiState.cache as List<AlmacenItemDomain>

                            itemsContent(
                                items = items,
                                showMenu = false,
                                showAdminOptions = false,
                                onAdd = { _ -> },
                                onEdit = { _ -> },
                                onRemove = { _ -> },
                                onTake = { _ -> }
                            )
                        }

                        is UiState.Success<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val items = uiState.value as List<AlmacenItemDomain>

                            itemsContent(
                                items = items,
                                showMenu = true,
                                showAdminOptions = viewModel.loggedUser.role == UserRoleDomain.ADMIN,
                                onTake = viewModel::setTakeStock,
                                onAdd = viewModel::setAddStock,
                                onEdit = viewModel::setEdit,
                                onRemove = viewModel::setDelete
                            )
                        }

                        is UiState.Error<*> -> {
                            stickyHeader { _ ->
                                var expandError: Boolean by remember { mutableStateOf(false) }
                                val sheetState: SheetState = rememberModalBottomSheetState(
                                    skipPartiallyExpanded = false
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 8.dp)
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .combinedClickable(
                                            onClick = {},
                                            onLongClick = { expandError = true },
                                            onLongClickLabel = "Expand error message"
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) errColumn@{
                                    Text(text = "Error de conexión")

                                    if (uiState.retry) {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 2.dp)
                                        )
                                    }

                                    if (!expandError) return@errColumn
                                    val scrollState: ScrollState = rememberScrollState()

                                    ModalBottomSheet(
                                        modifier = Modifier.fillMaxSize(),
                                        sheetState = sheetState,
                                        onDismissRequest = { expandError = false }
                                    ) {
                                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                                            Text(
                                                text = uiState.cause.message
                                                    ?: "No message provided"
                                            )
                                            Text(text = uiState.cause.stackTraceToString())
                                        }
                                    }
                                }
                            }

                            @Suppress("UNCHECKED_CAST")
                            val items = uiState.cache as List<AlmacenItemDomain>

                            itemsContent(
                                items = items,
                                showMenu = false,
                                showAdminOptions = false,
                                onTake = {},
                                onAdd = {},
                                onEdit = {},
                                onRemove = {}
                            )

                            Log.e("AlmacenScreen", "Error fetching items", uiState.cause)
                        }
                    }
                }
            }

            val itemManagementState: ItemManagementUiState by viewModel.itemManagementUiState
                .collectAsStateWithLifecycle()

            when (itemManagementState) {
                ItemManagementUiState.Idle -> Unit

                is ItemManagementUiState.CreateItem -> {
                    CreateItemBottomSheet(
                        viewModel = koinViewModel<CreateItemViewModel>(),
                        itemState = (itemManagementState as ItemManagementUiState.CreateItem).state,
                        onCreate = viewModel::onCreate,
                        onDismiss = viewModel::clearItemManagementUiState,
                        onUnauthorized = {
                            UnauthorizedDialog(
                                onAccept = {
                                    viewModel.clearItemManagementUiState()
                                    onNavigateBack()
                                }
                            )
                        },
                        onNotFound = {
                            snackbarHost.showSnackbar(
                                message = "Error: El item no existe en el servidor",
                                actionLabel = "OK"
                            )
                        },
                        onForbidden = {
                            snackbarHost.showSnackbar(
                                message = "Error: No tienes permisos para crear un item",
                                actionLabel = "OK"
                            )
                        }
                    )
                }

                is ItemManagementUiState.AddStock -> {
                    val (
                        item: AlmacenItemDomain,
                        itemState: ItemUiState
                    ) = (itemManagementState as ItemManagementUiState.AddStock)
                        .let { it.item to it.state }

                    AddStockBottomSheet(
                        viewModel = koinViewModel<AddStockViewModel>(
                            key = item.hashCode().toString(),
                            parameters = {
                                parametersOf(item)
                            }
                        ),
                        itemState = itemState,
                        onAddStock = viewModel::onAddStock,
                        onDismiss = viewModel::clearItemManagementUiState,
                        onUnauthorized = {
                            UnauthorizedDialog(
                                onAccept = {
                                    viewModel.clearItemManagementUiState()
                                    onNavigateBack()
                                }
                            )
                        },
                        onNotFound = {
                            snackbarHost.showSnackbar(
                                message = "Error: El item '${item.name}' no existe en el servidor",
                                actionLabel = "OK"
                            )
                        }
                    )
                }

                is ItemManagementUiState.TakeStock -> {
                    val (
                        item: AlmacenItemDomain,
                        itemState: ItemUiState
                    ) = (itemManagementState as ItemManagementUiState.TakeStock)
                        .let { it.item to it.state }

                    TakeStockBottomSheet(
                        viewModel = koinViewModel<TakeStockViewModel>(
                            key = item.hashCode().toString(),
                            parameters = {
                                parametersOf(item)
                            }
                        ),
                        itemState = itemState,
                        onTakeStock = viewModel::onTakeStock,
                        onDismiss = viewModel::clearItemManagementUiState,
                        onUnauthorized = {
                            UnauthorizedDialog(
                                onAccept = {
                                    viewModel.clearItemManagementUiState()
                                    onNavigateBack()
                                }
                            )
                        },
                        onNotFound = {
                            snackbarHost.showSnackbar(
                                message = "Error: El item '${item.name}' no existe en el servidor",
                                actionLabel = "OK"
                            )
                        }
                    )
                }

                is ItemManagementUiState.Edit -> {
                    val (
                        item: AlmacenItemDomain,
                        itemState: ItemUiState
                    ) = (itemManagementState as ItemManagementUiState.Edit)
                        .let { it.item to it.state }

                    EditBottomSheet(
                        viewModel = koinViewModel<EditItemViewModel>(
                            key = item.hashCode().toString(),
                            parameters = {
                                parametersOf(item)
                            }
                        ),
                        itemState = itemState,
                        onEdit = viewModel::onEdit,
                        onDismiss = viewModel::clearItemManagementUiState,
                        onUnauthorized = {
                            UnauthorizedDialog(
                                onAccept = {
                                    viewModel.clearItemManagementUiState()
                                    onNavigateBack()
                                }
                            )
                        },
                        onNotFound = {
                            snackbarHost.showSnackbar(
                                message = "Error: El item '${item.name}' no existe en el servidor",
                                actionLabel = "OK"
                            )
                        },
                        onForbidden = {
                            snackbarHost.showSnackbar(
                                message = "Error: No tienes permisos para editar este item",
                                actionLabel = "OK"
                            )
                        }
                    )
                }

                is ItemManagementUiState.ToBeDeleted -> {

                    val (
                        item: AlmacenItemDomain,
                        itemState: ItemUiState
                    ) = (itemManagementState as ItemManagementUiState.ToBeDeleted)
                        .let { it.item to it.state }

                    DeleteItemDialog(
                        item = item,
                        itemState = itemState,
                        onDismissRequest = viewModel::clearItemManagementUiState,
                        onDelete = viewModel::onDelete,
                        onUnauthorized = {
                            UnauthorizedDialog(
                                onAccept = {
                                    viewModel.clearItemManagementUiState()
                                    onNavigateBack()
                                }
                            )
                        },
                        onNotFound = {
                            snackbarHost.showSnackbar(
                                message = "Error: El item '${item.name}' no existe en el servidor",
                                actionLabel = "OK"
                            )
                        },
                        onForbidden = {
                            snackbarHost.showSnackbar(
                                message = "Error: No tienes permisos para borrar este item",
                                actionLabel = "OK"
                            )
                        }
                    )
                }

                is ItemManagementUiState.UnexpectedDeleted -> {
                    LaunchedEffect(Unit) {
                        val item: AlmacenItemDomain =
                            (itemManagementState as ItemManagementUiState.UnexpectedDeleted).item

                        snackbarHost.showSnackbar(
                            message = "Se ha eliminado '${item.name}'",
                            actionLabel = "OK",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier,
    viewModel: AlmacenViewModel
) {
    val filterText: String = when (viewModel.filterMode) {
        FilterMode.NAME -> "nombre"
        FilterMode.BARCODE -> "codigo de barras"
        FilterMode.SUPPLIER -> "proveedor"
    }

    OutlinedTextField(
        modifier = modifier,
        value = viewModel.filter,
        onValueChange = viewModel::updateFilter,
        label = { Text(text = "Filtrar por $filterText") },
        singleLine = true,
        leadingIcon = {
            Box {
                IconButton(onClick = { viewModel.updateShowFilter(viewModel.showFilter.not()) }) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.filter_alt_24dp),
                        contentDescription = null
                    )
                }

                DropdownMenu(
                    expanded = viewModel.showFilter,
                    onDismissRequest = { viewModel.updateShowFilter(false) }
                ) {
                    FilterMode.entries.forEach { filterMode: FilterMode ->
                        val filterLabel: String = when (filterMode) {
                            FilterMode.NAME -> "Nombre"
                            FilterMode.BARCODE -> "Código de barras"
                            FilterMode.SUPPLIER -> "Proveedor"
                        }

                        val imageVector: ImageVector = when (filterMode) {
                            FilterMode.NAME -> vectorResource(Res.drawable.abc_24dp)
                            FilterMode.BARCODE -> vectorResource(Res.drawable.barcode_24dp)
                            FilterMode.SUPPLIER -> vectorResource(Res.drawable.delivery_truck_speed_24dp)
                        }

                        DropdownMenuItem(
                            text = { Text(text = filterLabel) },
                            leadingIcon = {
                                Icon(
                                    imageVector = imageVector,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                viewModel.updateFilterMode(filterMode)
                                viewModel.updateShowFilter(false)
                            }
                        )
                    }
                }
            }
        },
        trailingIcon = {
            IconButton(onClick = { viewModel.updateFilter("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null
                )
            }
        }
    )
}

private fun LazyGridScope.itemsContent(
    items: List<AlmacenItemDomain>,
    showMenu: Boolean,
    showAdminOptions: Boolean,
    onTake: (AlmacenItemDomain) -> Unit,
    onAdd: (AlmacenItemDomain) -> Unit,
    onEdit: (AlmacenItemDomain) -> Unit,
    onRemove: (AlmacenItemDomain) -> Unit
) {
    if (items.isNotEmpty()) {
        items(
            items = items,
            key = AlmacenItemDomain::id
        ) { item ->
            Item(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp),
                item = item,
                showMenu = showMenu,
                showAdminOptions = showAdminOptions,
                onTake = { onTake(item) },
                onAdd = { onAdd(item) },
                onEdit = { onEdit(item) },
                onRemove = { onRemove(item) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }

    } else {
        stickyHeader {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "No hay elementos disponibles"
                )
            }
        }
    }
}
