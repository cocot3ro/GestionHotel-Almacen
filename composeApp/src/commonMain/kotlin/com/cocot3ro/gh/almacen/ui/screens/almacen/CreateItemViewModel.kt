package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CreateItemViewModel : ViewModel() {

    var barcodes: MutableSet<Long> = mutableStateSetOf()
        private set
    var name: String by mutableStateOf("")
        private set
    var supplier: String? by mutableStateOf(null)
        private set
    var image: String? by mutableStateOf(null)
        private set
    var quantity: Int? by mutableStateOf(null)
        private set
    var packSize: Int? by mutableStateOf(null)
        private set
    var minimum: Int? by mutableStateOf(null)
        private set

    var newImageData: Pair<ByteArray, String>? by mutableStateOf(null)
        private set

    var showBarcodeInput: Boolean by mutableStateOf(false)
        private set
    var newBarcodeInput: String by mutableStateOf("")
        private set

    var newImageTempUri: String? by mutableStateOf(null)
        private set
    var showImageSelection: Boolean by mutableStateOf(false)
        private set

    fun addBarcode(barcode: Long): Boolean = this.barcodes.add(barcode)

    fun removeBarcode(barcode: Long) {
        this.barcodes -= barcode
    }

    fun updateName(name: String) {
        this.name = name.trimStart()
    }

    fun updateSupplier(supplier: String) {
        this.supplier = supplier.trimStart()
    }

    fun updateImage(image: String, imageData: Pair<ByteArray, String>) {
        this.image = image
        this.newImageData = imageData
    }

    fun removeImage() {
        this.newImageData = null
        this.image = null
    }

    fun updateQuantity(quantity: String) {
        val value: Int? = quantity.replace("""\D""".toRegex(), "").toIntOrNull()
        this.quantity = value
    }

    fun updatePackSize(packSize: String) {
        val value: Int? = packSize.replace("""\D""".toRegex(), "").toIntOrNull()
        this.packSize = value
    }

    fun updateMinimum(minimum: String) {
        val value: Int? = minimum.replace("""\D""".toRegex(), "").toIntOrNull()
        this.minimum = value
    }

    fun toggleShowbarcodeInput() {
        this.showBarcodeInput = !showBarcodeInput
        this.newBarcodeInput = ""
    }

    fun updateNewBarcodeInput(newBarcodeInput: String) {
        this.newBarcodeInput = newBarcodeInput
    }

    fun updateNewImageTempUri(newImageTempUri: String?) {
        this.newImageTempUri = newImageTempUri
    }

    fun updateShowImageSelection(showImageSelection: Boolean) {
        this.showImageSelection = showImageSelection
    }

    fun getItem(): AlmacenItemDomain = AlmacenItemDomain(
        barcodes = this.barcodes.toLongArray(),
        name = this.name,
        supplier = this.supplier,
        quantity = this.quantity!!,
        packSize = this.packSize!!,
        minimum = this.minimum!!,
        image = null
    )

    fun isValidForm(): Boolean {
        return this.name.isBlank().not() &&
                this.quantity != null &&
                this.packSize != null &&
                this.minimum != null &&
                this.quantity?.let { it >= 0 } ?: false &&
                this.packSize?.let { it > 0 } ?: false &&
                this.minimum?.let { it >= 0 } ?: false
    }

    fun dismiss() {
        barcodes.clear()
        name = ""
        supplier = null
        image = null
        quantity = null
        packSize = null
        minimum = null

        showBarcodeInput = false
        newBarcodeInput = ""

        newImageTempUri = null
        newImageData = null

        showImageSelection = false
    }
}