package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.barcode_24dp
import gh_almacen.composeapp.generated.resources.broken_image_24dp
import gh_almacen.composeapp.generated.resources.delivery_truck_speed_24dp
import gh_almacen.composeapp.generated.resources.security_24dp
import gh_almacen.composeapp.generated.resources.trolley_48dp
import gh_almacen.composeapp.generated.resources.visibility_24dp
import gh_almacen.composeapp.generated.resources.visibility_off_24dp
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
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
        ) {
            var showBarcodeList: Boolean by remember { mutableStateOf(false) }

            Row(modifier = Modifier.fillMaxWidth()) {
                if (item.image != null) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(100.dp),
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

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (item.quantity <= item.minimum) {
                        val modId = "lockIcon"
                        val text: AnnotatedString = buildAnnotatedString {
                            appendInlineContent(id = modId, alternateText = "[icon]")
                            append(' ')
                            append(text = item.name)
                        }

                        val inlineContent: Map<String, InlineTextContent> = mapOf(
                            pair = Pair(
                                first = modId,
                                second = InlineTextContent(
                                    placeholder = Placeholder(
                                        width = 20.sp,
                                        height = 20.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                    ),
                                    children = {
                                        Icon(
                                            modifier = Modifier.fillMaxSize(),
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null
                                        )
                                    }
                                )
                            )
                        )

                        Text(
                            text = text,
                            inlineContent = inlineContent,
                            fontSize = 22.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = item.name,
                            fontSize = 22.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (item.supplier != null) {
                        val modId = "lockIcon"
                        val text: AnnotatedString = buildAnnotatedString {
                            appendInlineContent(id = modId, alternateText = "[icon]")
                            append(' ')
                            append(text = item.supplier)
                        }

                        val inlineContent: Map<String, InlineTextContent> = mapOf(
                            pair = Pair(
                                first = modId,
                                second = InlineTextContent(
                                    placeholder = Placeholder(
                                        width = 20.sp,
                                        height = 20.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                    ),
                                    children = {
                                        Icon(
                                            modifier = Modifier.fillMaxSize(),
                                            imageVector = vectorResource(Res.drawable.delivery_truck_speed_24dp),
                                            contentDescription = null
                                        )
                                    }
                                )
                            )
                        )

                        Text(
                            text = text,
                            inlineContent = inlineContent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                Column {


                    Box dropDownBox@{
                        var showDropDown: Boolean by remember { mutableStateOf(false) }

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

                            HorizontalDivider()

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        imageVector = vectorResource(Res.drawable.barcode_24dp),
                                        contentDescription = null
                                    )
                                },
                                text = {
                                    Text(text = "Mostrar códigos${System.lineSeparator()}de barras")
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showBarcodeList) {
                                            vectorResource(Res.drawable.visibility_24dp)
                                        } else {
                                            vectorResource(Res.drawable.visibility_off_24dp)
                                        },
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showDropDown = false
                                    showBarcodeList = !showBarcodeList
                                }
                            )

                            if (!showAdminOptions) return@dropDownMenuContent

                            HorizontalDivider()

                            val modId = "secureIcon"
                            val text: AnnotatedString = buildAnnotatedString {
                                appendInlineContent(id = modId, alternateText = "[icon]")
                                append(' ')
                                append(text = "Administración")
                            }

                            val inlineContent: Map<String, InlineTextContent> = mapOf(
                                pair = Pair(
                                    first = modId,
                                    second = InlineTextContent(
                                        placeholder = Placeholder(
                                            width = 20.sp,
                                            height = 20.sp,
                                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                                        ),
                                        children = {
                                            Icon(
                                                modifier = Modifier.fillMaxSize(),
                                                imageVector = vectorResource(Res.drawable.security_24dp),
                                                contentDescription = null
                                            )
                                        }
                                    )
                                )
                            )

                            Text(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = text,
                                inlineContent = inlineContent
                            )

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
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Text(text = "En stock: ${item.quantity}")

                VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                Text(text = "Uds. pack: ${item.packSize}")
            }

            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                visible = showBarcodeList
            ) {
                if (!showBarcodeList) return@AnimatedVisibility

                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = item.barcodes.toTypedArray(),
                        key = { barcode: Long -> barcode }
                    ) { barcode: Long ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(text = "$barcode")
                            }
                        )
                    }
                }
            }
        }
    }
}
