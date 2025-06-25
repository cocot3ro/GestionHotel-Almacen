package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain

fun AlmacenItemModel.toDomain() = AlmacenItemDomain(
    id = id,
    barcodes = barcodes,
    name = name,
    supplier = supplier,
    image = image,
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)