@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.screens.almacen

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain
import com.cocot3ro.gh.almacen.ui.dialogs.UnauthorizedDialog
import com.cocot3ro.gh.almacen.ui.state.UiState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlmacenScreen(
    modifier: Modifier,
    viewModel: AlmacenViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState: UiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val snackbarHost: SnackbarHostState = remember { SnackbarHostState() }

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
                        text = viewModel.loggedUser.name,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {

                }
            )
        },
        floatingActionButton = {
//            val navBarsPadding: PaddingValues = WindowInsets.navigationBars
//                .only(WindowInsetsSides.Horizontal)
//                .asPaddingValues()
//
//            FloatingActionButton(
//                modifier = Modifier.padding(navBarsPadding),
//                onClick = onNavigateToCarrito
//            ) {
//                Icon(
//                    modifier = Modifier.size(36.dp),
//                    imageVector = vectorResource(Res.drawable.trolley_48dp),
//                    contentDescription = null
//                )
//            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHost)
        }
    ) { innerPadding ->

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            isRefreshing = uiState is UiState.Reloading,
            onRefresh = viewModel::refresh
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 350.dp),
                userScrollEnabled = uiState !is UiState.Loading,
                contentPadding = PaddingValues(all = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (uiState) {
                    is UiState.Idle -> Unit
                    is UiState.Loading -> {
                        items(count = 50) { _ ->
                            ItemShimmer(modifier = Modifier.fillMaxSize())
                        }
                    }

                    is UiState.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val items = uiState.value as List<AlmacenItemDomain>

                        if (items.isNotEmpty()) {
                            items(items) { item ->
                                Item(
                                    modifier = Modifier.fillMaxSize(),
                                    item = item,
                                    showMenu = true,
                                    showAdminOptions = viewModel.loggedUser.role == UserRoleDomain.ADMIN,
                                    onTake = { viewModel.setTakeStock(item) },
                                    onAdd = { viewModel.setAddStock(item) },
                                    onEdit = { viewModel.setEdit(item) },
                                    onRemove = { viewModel.setDelete(item) }
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
                            ) {
                                Text(text = "Error de conexión")

                                if (uiState.retry) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp)
                                    )
                                }

                                if (!expandError) return@Column
                                val scrollState: ScrollState = rememberScrollState()

                                ModalBottomSheet(
                                    modifier = Modifier.fillMaxSize(),
                                    sheetState = sheetState,
                                    onDismissRequest = { expandError = false }
                                ) {
                                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                                        Text(text = uiState.cause.message ?: "No message provided")
                                        Text(text = uiState.cause.stackTraceToString())
                                    }
                                }
                            }
                        }

                        @Suppress("UNCHECKED_CAST")
                        val items = uiState.cache as List<AlmacenItemDomain>

                        if (items.isNotEmpty()) {
                            items(items) { item ->
                                Item(
                                    modifier = Modifier.fillMaxSize(),
                                    item = item,
                                    showMenu = false,
                                    showAdminOptions = false,
                                    onTake = {},
                                    onAdd = {},
                                    onEdit = {},
                                    onRemove = {}
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

                        Log.e("AlmacenScreen", "Error fetching items", uiState.cause)
                    }

                    else -> Unit
                }
            }
        }

        val itemManagementState: ItemManagementUiState by viewModel.itemManagementUiState
            .collectAsStateWithLifecycle()

        when (itemManagementState) {
            ItemManagementUiState.Idle -> Unit

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
                    onDissmiss = viewModel::clearItemManagementUiState,
                    onUnauthrized = {
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
                    onDissmiss = viewModel::clearItemManagementUiState,
                    onUnauthrized = {
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
                    onDissmiss = viewModel::clearItemManagementUiState,
                    onUnauthrized = {
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
