package com.cocot3ro.gh.almacen.ui.screens.almacen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cocot3ro.gh.almacen.domain.model.SortMode
import com.cocot3ro.gh.almacen.domain.state.UiState
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.abc_24dp
import gh_almacen.composeapp.generated.resources.format_list_bulleted_24dp
import gh_almacen.composeapp.generated.resources.format_list_numbered_24dp
import gh_almacen.composeapp.generated.resources.format_list_numbered_rtl_24dp
import gh_almacen.composeapp.generated.resources.search_off_24dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlmacenTopAppBar(
    viewModel: AlmacenViewModel,
    uiState: UiState,
    drawerState: DrawerState,
    lazyGridState: LazyGridState
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
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
                    AlmacenSearchBar(
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
}