package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.util.fastCoerceIn
import androidx.lifecycle.ViewModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class EditItemViewModel(
    @InjectedParam private val originalItem: AlmacenItemDomain
) : ViewModel() {

    private val _barcodes: MutableSet<Long> =
        mutableStateSetOf(*originalItem.barcodes.toTypedArray())
    val barcodes: Set<Long> get() = (_barcodes as SnapshotStateSet<Long>).toSet()

    var name: String by mutableStateOf(originalItem.name)
        private set
    var supplier: String? by mutableStateOf(originalItem.supplier)
        private set
    var image: String? by mutableStateOf(originalItem.image)
        private set
    var quantity: Int? by mutableStateOf(originalItem.quantity)
        private set
    var packSize: Int? by mutableStateOf(originalItem.packSize)
        private set
    var minimum: Int? by mutableStateOf(originalItem.minimum)
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

    fun addBarcode(barcode: Long): Boolean = this._barcodes.add(barcode)

    fun removeBarcode(barcode: Long) {
        this._barcodes -= barcode
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
        val value: Int? = quantity.replace("""\D""".toRegex(), "")
            .toLongOrNull()
            ?.fastCoerceIn(0, Int.MAX_VALUE.toLong())
            ?.toInt()
        this.quantity = value
    }

    fun updatePackSize(packSize: String) {
        val value: Int? = packSize.replace("""\D""".toRegex(), "")
            .toLongOrNull()
            ?.fastCoerceIn(0, Int.MAX_VALUE.toLong())
            ?.toInt()
        this.packSize = value
    }

    fun updateMinimum(minimum: String) {
        val value: Int? = minimum.replace("""\D""".toRegex(), "")
            .toLongOrNull()
            ?.fastCoerceIn(0, Int.MAX_VALUE.toLong())
            ?.toInt()
        this.minimum = value
    }

    fun toggleShowBarcodeInput() {
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

    fun getItem(): AlmacenItemDomain = originalItem.copy(
        barcodes = this._barcodes.toLongArray(),
        name = this.name,
        supplier = this.supplier,
        image = originalItem.image.takeUnless { image == null },
        quantity = this.quantity!!,
        packSize = this.packSize!!,
        minimum = this.minimum
    )

    fun isValidForm(): Boolean {
        return this.name.isNotBlank() &&
                this.quantity?.let { it >= 0 } ?: false &&
                this.packSize?.let { it > 0 } ?: false &&
                this.minimum?.let { it >= 0 } ?: true
    }

    fun dismiss() {
        _barcodes.clear()
        _barcodes.addAll(originalItem.barcodes.toList())
        name = originalItem.name
        supplier = originalItem.supplier
        image = originalItem.image
        quantity = originalItem.quantity
        packSize = originalItem.packSize
        minimum = originalItem.minimum

        showBarcodeInput = false
        newBarcodeInput = ""

        newImageTempUri = null
        newImageData = null

        showImageSelection = false
    }
}
