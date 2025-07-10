package com.cocot3ro.gh.almacen.domain.state

sealed class ItemUiState {
    data object Idle : ItemUiState()
    data object Waiting : ItemUiState()
    data object Loading : ItemUiState()
    data object Success : ItemUiState()
    data class Error(val cause: Throwable) : ItemUiState()
}