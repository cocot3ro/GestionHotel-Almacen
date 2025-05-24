package com.cocot3ro.gh.almacen.ui.screens.almacen

import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain

sealed class ItemUiState {
    data object Idle : ItemUiState()
    data class Success(val item: AlmacenItemDomain, val hasChanged: Boolean) : ItemUiState()
    data class Deleted(val item: AlmacenItemDomain) : ItemUiState()
}