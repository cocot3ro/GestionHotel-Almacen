package com.cocot3ro.gh.almacen.ui.screens

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success<T>(val value: T) : UiState()
    data class Error(val throwable: Throwable) : UiState()
}