package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastCoerceIn
import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class TakeStockViewModel(
    @InjectedParam val item: AlmacenItemDomain
) : ViewModel() {
    var min: Int by mutableStateOf(0)
        private set

    var amount: Int by mutableStateOf(0)
        private set

    var max: Int by mutableStateOf(item.quantity)
        private set

    fun updateAmount(input: String) {
        val amount: Int = input.replace("""\D""".toRegex(), replacement = "").toIntOrNull() ?: 0
        updateAmount(amount)
    }

    private fun updateAmount(amount: Int) {
        this.amount = amount.fastCoerceIn(minimumValue = min, maximumValue = max)
    }

    fun incrementAmount(): Unit = updateAmount(amount + 1)
    fun decrementAmount(): Unit = updateAmount(amount - 1)

    fun dismiss() {
        this.amount = 0
    }
}
