package com.cocot3ro.gh.almacen.domain.state.ext

import com.cocot3ro.gh.almacen.domain.state.UiState

fun UiState.isLoadingOrReloading(): Boolean {
    return this is UiState.Loading || this is UiState.Reloading<*>
}