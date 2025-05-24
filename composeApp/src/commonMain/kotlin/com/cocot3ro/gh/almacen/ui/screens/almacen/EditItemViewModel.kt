package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class EditItemViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<ItemUiState> = MutableStateFlow(ItemUiState.Idle)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    private var barcode: Long? by mutableStateOf(null)
    private var name: String? by mutableStateOf(null)
    private var supplier: String? by mutableStateOf(null)
    private var image: String? by mutableStateOf(null)
    private var quantity: Int? by mutableStateOf(null)
    private var packSize: Int? by mutableStateOf(null)
    private var minimum: Int? by mutableStateOf(null)

    fun updateItem(item: AlmacenItemDomain?) {
        when (_uiState.value) {
            ItemUiState.Idle -> {
                _uiState.value = ItemUiState.Success(item = item!!, hasChanged = false)
            }

            is ItemUiState.Success -> {
                if (item != null) {
                    _uiState.value = ItemUiState.Success(item = item, hasChanged = true)
//                    this.max = Int.MAX_VALUE - item.quantity
//                    updateAmount(this.amount)
                } else {
                    _uiState.value =
                        ItemUiState.Deleted((_uiState.value as ItemUiState.Success).item)
//                    this.min = null
//                    this.max = null
//                    this.amount = 0
                }
            }

            is ItemUiState.Deleted -> Unit
        }
    }

}
