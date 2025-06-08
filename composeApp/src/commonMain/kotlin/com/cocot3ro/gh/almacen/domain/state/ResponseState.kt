package com.cocot3ro.gh.almacen.domain.state

sealed class ResponseState {
    data class OK<T>(val data: T) : ResponseState()
    data class Created<T>(val data: T) : ResponseState()
    data object NoContent : ResponseState()

    data object BadRequest : ResponseState()
    data object NotFound : ResponseState()
    data object Unauthorized : ResponseState()
    data object Forbidden : ResponseState()

    //    data object ServiceUnavailable : ResponseState()
    data class Error(val cause: Throwable) : ResponseState()
}
