package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.delivery_truck_speed_24dp
import gh_almacen.composeapp.generated.resources.left_panel_close_24dp
import gh_almacen.composeapp.generated.resources.logout_24dp
import gh_almacen.composeapp.generated.resources.trolley_24dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlmacenDrawer(
    viewModel: AlmacenViewModel,
    drawerState: DrawerState,
    onNavigateBack: () -> Unit,
    onNavigateToCarga: () -> Unit,
    onNavigateToDescarga: () -> Unit
) {

    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    ModalDrawerSheet {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(20.dp),
                    tooltip = {
                        PlainTooltip {
                            Text(text = "Cerrar sesión")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = {
                        onNavigateBack()
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.logout_24dp),
                            contentDescription = null
                        )
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(20.dp),
                    tooltip = {
                        PlainTooltip {
                            Text(text = "Cerrar panel")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.left_panel_close_24dp),
                            contentDescription = null
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.loggedUser.image != null) {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .aspectRatio(1f),
                        model = viewModel.loggedUser.image,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .aspectRatio(1f),
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null
                    )
                }

                Text(
                    text = viewModel.loggedUser.name,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                )
            }
        }

        HorizontalDivider()

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = vectorResource(Res.drawable.delivery_truck_speed_24dp),
                    contentDescription = null
                )
            },
            label = {
                Text(text = "Carga")
            },
            selected = false,
            onClick = {
                onNavigateToCarga()
                coroutineScope.launch {
                    drawerState.close()
                }
            },
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = vectorResource(Res.drawable.trolley_24dp),
                    contentDescription = null
                )
            },
            label = {
                Text(text = "Descarga")
            },
            selected = false,
            onClick = {
                onNavigateToDescarga()
                coroutineScope.launch {
                    drawerState.close()
                }
            },
        )

//            HorizontalDivider()
//
//            NavigationDrawerItem(
//                icon = {
//                    Icon(
//                        imageVector = Icons.Default.Settings,
//                        contentDescription = null
//                    )
//                },
//                label = {
//                    Text(text = "Configuración")
//                },
//                selected = false,
//                onClick = onNavigateToSettings,
//            )
    }
}