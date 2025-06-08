package com.cocot3ro.gh.almacen.ui.state

sealed class UiState {
    data object Idle : UiState()

    data object Loading : UiState()
    data object Reloading : UiState()

    data class Success<T>(val value: T) : UiState()

    data class Error<T>(
        val cause: Throwable,
        val retry: Boolean,
        val cache: T
    ) : UiState()
}