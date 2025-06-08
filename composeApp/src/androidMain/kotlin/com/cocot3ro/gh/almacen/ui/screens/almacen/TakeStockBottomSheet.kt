package com.cocot3ro.gh.almacen.ui.screens.almacen

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cocot3ro.gh.almacen.domain.state.ex.NotFoundException
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.remove_24dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeStockBottomSheet(
    viewModel: TakeStockViewModel,
    itemState: ItemUiState,
    onTakeStock: (Int) -> Unit,
    onDissmiss: () -> Unit,
    onUnauthrized: @Composable () -> Unit,
    onNotFound: suspend () -> Unit
) {
    val sheetState: SheetState = rememberModalBottomSheetState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest@{
            if (itemState is ItemUiState.Loading) return@onDismissRequest

            viewModel.dissmiss()
            onDissmiss()
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = viewModel.item.name,
                    fontSize = 28.sp
                )

                TextButton(
                    onClick = onClick@{
                        if (itemState is ItemUiState.Loading) return@onClick

                        coroutineScope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.dissmiss()
                                    onDissmiss()
                                }
                            }
                    }
                ) {
                    Text("Cancelar")
                }
            }

            Text(
                text = "Stock actual: ${viewModel.item.quantity}",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val interactionSource = remember { MutableInteractionSource() }

                FilledTonalIconButton(
                    enabled = itemState !is ItemUiState.Idle &&
                            itemState !is ItemUiState.Loading &&
                            viewModel.amount > viewModel.min,
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
                    enabled = itemState !is ItemUiState.Idle && itemState !is ItemUiState.Loading,
                    label = { Text(text = "Cantidad") },
                    value = viewModel.amount.toString(),
                    onValueChange = viewModel::updateAmount,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    interactionSource = interactionSource
                )

                FilledTonalIconButton(
                    enabled = itemState !is ItemUiState.Idle &&
                            itemState !is ItemUiState.Loading &&
                            viewModel.amount < viewModel.max,
                    onClick = viewModel::incrementAmount
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            AnimatedVisibility(
                visible = itemState is ItemUiState.Loading,
                enter = slideInVertically { it },
                exit = slideOutVertically { -it },
                content = { LinearProgressIndicator() }
            )

            Button(
                modifier = Modifier.align(Alignment.End),
                enabled = itemState !is ItemUiState.Idle &&
                        itemState !is ItemUiState.Loading &&
                        viewModel.amount.let { it >= viewModel.min && it <= viewModel.max },
                onClick = { onTakeStock(viewModel.amount) }
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 20.sp
                )
            }
        }
    }

    when (itemState) {
        is ItemUiState.Error -> {
            when (itemState.cause) {
                is UnauthorizedException -> onUnauthrized()

                is NotFoundException -> LaunchedEffect(Unit) {
                    launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                viewModel.dissmiss()
                                onDissmiss()
                                runBlocking { onNotFound() }
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
                            viewModel.dissmiss()
                            onDissmiss()

                            Toast.makeText(context, "Stock descontado", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        else -> Unit
    }
}
