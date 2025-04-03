package com.cocot3ro.gh.model.network.data.almacen

import kotlinx.serialization.Serializable

@Serializable
data class AlmacenModel(
    val id: Long = 0L,
    val name: String,
    val quantity: Int,
    val packSize: Int,
    val minimum: Int,
)