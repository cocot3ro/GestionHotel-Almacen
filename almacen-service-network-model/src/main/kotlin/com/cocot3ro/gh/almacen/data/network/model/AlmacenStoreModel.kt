package com.cocot3ro.gh.almacen.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AlmacenStoreModel(
    val id: Long = 0L,
    val name: String,
    val image: String?
)
