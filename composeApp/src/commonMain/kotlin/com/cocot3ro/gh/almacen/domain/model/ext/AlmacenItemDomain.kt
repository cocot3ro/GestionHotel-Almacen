package com.cocot3ro.gh.almacen.domain.model.ext

import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain

fun AlmacenItemDomain.toModel() = AlmacenItemModel(
    id = id,
    barcodes = barcodes,
    name = name,
    supplier = supplier,
    image = image?.replace("""^https?://[^/]+""".toRegex(), ""),
    quantity = quantity,
    packSize = packSize,
    minimum = minimum
)