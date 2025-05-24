package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastCoerceIn
import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.usecase.ManageAlmacenItemUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class AddStockViewModel(
    @Provided private val manageAlmacenItemUseCase: ManageAlmacenItemUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<ItemUiState> = MutableStateFlow(ItemUiState.Idle)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    var min: Int? by mutableStateOf(0)
        private set

    var amount: Int? by mutableStateOf(0)
        private set

    var max: Int? by mutableStateOf(Int.MAX_VALUE)
        private set

    fun setItem(item: AlmacenItemDomain?) {
        when (_uiState.value) {
            ItemUiState.Idle -> {
                _uiState.value = ItemUiState.Success(item = item!!, hasChanged = false)
            }

            is ItemUiState.Success -> {
                if (item != null) {
                    _uiState.value = ItemUiState.Success(item = item, hasChanged = true)
                    this.max = Int.MAX_VALUE - item.quantity
                    updateAmount(this.amount)
                } else {
                    _uiState.value =
                        ItemUiState.Deleted((_uiState.value as ItemUiState.Success).item)
                    this.min = null
                    this.max = null
                    this.amount = 0
                }
            }

            is ItemUiState.Deleted -> Unit
        }
    }

    fun updateAmount(input: String) {
        val amount: Int? = input.replace("""\D""".toRegex(), replacement = "").toIntOrNull()
        updateAmount(amount)
    }

    private fun updateAmount(amount: Int?) {
        when (_uiState.value) {
            ItemUiState.Idle -> Unit

            is ItemUiState.Success -> {
                this.amount = amount?.fastCoerceIn(minimumValue = min!!, maximumValue = max!!)
            }

            is ItemUiState.Deleted -> Unit
        }
    }

    fun incrementAmount(): Unit = updateAmount(amount?.let { it + 1 } ?: 1)
    fun decrementAmount(): Unit = updateAmount(amount?.let { it - 1 } ?: Int.MAX_VALUE)

    fun dissmiss() {
        this.amount = 0
        _uiState.value = ItemUiState.Idle
    }

    suspend fun takeStock() {
        withContext(Dispatchers.IO) {
            when (val v: ItemUiState = _uiState.value) {
                ItemUiState.Idle -> Unit

                is ItemUiState.Success -> {
                    manageAlmacenItemUseCase.addStock(v.item, amount!!)
                }

                is ItemUiState.Deleted -> Unit
            }
        }
    }

}
