package com.cocot3ro.gh.almacen.ui.screens.almacen

import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain

sealed class ItemManagementUiState {

    data object Idle : ItemManagementUiState()

    data class CreateItem(val state: ItemUiState) : ItemManagementUiState()

    data class TakeStock(val item: AlmacenItemDomain, val state: ItemUiState) :
        ItemManagementUiState()

    data class AddStock(val item: AlmacenItemDomain, val state: ItemUiState) :
        ItemManagementUiState()

    data class Edit(val item: AlmacenItemDomain, val state: ItemUiState) : ItemManagementUiState()
    data class ToBeDeleted(val item: AlmacenItemDomain, val state: ItemUiState) :
        ItemManagementUiState()

    data class UnexpectedDeleted(val item: AlmacenItemDomain) : ItemManagementUiState()
}