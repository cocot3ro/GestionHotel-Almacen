package com.cocot3ro.gh.almacen.data.database.ext

import com.cocot3ro.gh.almacen.AlmacenStoreEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel

fun AlmacenStoreEntity.toModel(): AlmacenStoreModel = AlmacenStoreModel(
    id = id,
    name = name
)