package com.cocot3ro.gh.almacen.core.datastore.model.ext

import com.cocot3ro.gh.almacen.core.datastore.model.PreferenceCore
import com.cocot3ro.gh.almacen.domain.model.PreferenceItem

fun PreferenceCore.toDomain(): PreferenceItem = PreferenceItem(
    host = host,
    port = port
)