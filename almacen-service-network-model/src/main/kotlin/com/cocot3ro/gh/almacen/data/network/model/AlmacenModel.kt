package com.cocot3ro.gh.almacen.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AlmacenModel(
    val id: Long = 0L,
    val name: String,
    val quantity: Int,
    val packSize: Int,
    val minimum: Int,
)