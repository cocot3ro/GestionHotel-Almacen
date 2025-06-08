package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.broken_image_24dp
import gh_almacen.composeapp.generated.resources.delivery_truck_speed_24dp
import gh_almacen.composeapp.generated.resources.trolley_48dp
import org.jetbrains.compose.resources.vectorResource

@Composable
fun Item(
    modifier: Modifier,
    item: AlmacenItemDomain,
    showMenu: Boolean,
    showAdminOptions: Boolean,
    onTake: () -> Unit,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    var showDropDown: Boolean by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { _ ->
                    showDropDown = !showDropDown
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (item.image != null) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 8.dp),
                        model = item.image,
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
                }

                Text(
                    modifier = Modifier.weight(1f),
                    text = item.name,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.width(2.dp))

                Box dropDownBox@{
                    IconButton(onClick = { showDropDown = !showDropDown }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                        )
                    }

                    if (!showMenu) return@dropDownBox

                    DropdownMenu(
                        expanded = showDropDown,
                        onDismissRequest = { showDropDown = false }
                    ) dropDownMenuContent@{
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = vectorResource(Res.drawable.trolley_48dp),
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = "Traer")
                            },
                            onClick = {
                                showDropDown = false
                                onTake()
                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = vectorResource(Res.drawable.delivery_truck_speed_24dp),
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = "Agregar")
                            },
                            onClick = {
                                showDropDown = false
                                onAdd()
                            }
                        )

                        if (!showAdminOptions) return@dropDownMenuContent

                        HorizontalDivider()

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = "Editar")
                            },
                            onClick = {
                                showDropDown = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            },
                            text = {
                                Text(text = "Borrar")
                            },
                            onClick = {
                                showDropDown = false
                                onRemove()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(text = "En stock: ${item.quantity}")
        }
    }
}
