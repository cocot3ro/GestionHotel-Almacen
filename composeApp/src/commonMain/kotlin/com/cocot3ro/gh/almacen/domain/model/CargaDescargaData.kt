package com.cocot3ro.gh.almacen.domain.model

data class CargaDescargaData(
    val item: AlmacenItemDomain,
    val amount: Int?,
    val min: Int,
    val max: Int,
)
