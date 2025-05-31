package com.cocot3ro.gh.almacen.data.network.model.ext

import com.cocot3ro.gh.almacen.AlmacenStoreEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel

fun AlmacenStoreModel.toDatabase() = AlmacenStoreEntity(
    id = id,
    name = name
)