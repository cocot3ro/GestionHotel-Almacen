package com.cocot3ro.gh.almacen.domain.state

sealed class TestConnectionResult {

    data object Success : TestConnectionResult()
    data object ServiceUnavailable : TestConnectionResult()
    data class Error(val cause: Throwable) : TestConnectionResult()

}