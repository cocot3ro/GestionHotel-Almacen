package com.cocot3ro.gh.almacen.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlmacenUserPasswordChangeModel(
    @SerialName("current_password")
    val currentPassword: String?,

    @SerialName("new_password")
    val newPassword: String?
)
