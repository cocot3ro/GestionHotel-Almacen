package com.cocot3ro.gh.almacen.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlmacenItemModel(
    val id: Long = 0L,
    val barcodes: LongArray,
    val name: String,
    val supplier: String?,
    val image: String?,
    val quantity: Int,
    @SerialName("pack_size")
    val packSize: Int,
    val minimum: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlmacenItemModel

        return when {
            id != other.id -> false
            quantity != other.quantity -> false
            packSize != other.packSize -> false
            minimum != other.minimum -> false
            !barcodes.contentEquals(other.barcodes) -> false
            name != other.name -> false
            supplier != other.supplier -> false
            image != other.image -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + quantity
        result = 31 * result + packSize
        result = 31 * result + minimum
        result = 31 * result + barcodes.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (supplier?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        return result
    }
}