package com.cocot3ro.gh.almacen.domain.model

data class TextFieldValue<T>(
    val value: T,
    val status: TextFieldStatus,
)
