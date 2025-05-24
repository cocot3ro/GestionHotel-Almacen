package com.cocot3ro.gh.almacen.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlmacenLoginRequestModel(
    @SerialName("user_id")
    val userId: Long,

    val password: String?
)