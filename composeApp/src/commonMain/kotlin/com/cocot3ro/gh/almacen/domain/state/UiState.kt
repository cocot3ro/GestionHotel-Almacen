package com.cocot3ro.gh.almacen.domain.state

sealed class UiState {
    data object Idle : UiState()

    data object Loading : UiState()
    data class Reloading<T>(val cache: T) : UiState()

    data class Success<T>(val value: T) : UiState()

    data class Error<T>(
        val cause: Throwable,
        val retry: Boolean,
        val cache: T
    ) : UiState()
}