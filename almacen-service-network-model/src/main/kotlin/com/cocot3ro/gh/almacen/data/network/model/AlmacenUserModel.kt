package com.cocot3ro.gh.almacen.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlmacenUserModel(
    val id: Long = 0L,
    val name: String,
    val image: String?,
    @SerialName("requires_password")
    val requiresPassword: Boolean,
    val role: UserRoleModel
)