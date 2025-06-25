package com.cocot3ro.gh.almacen.domain.state.ex

class UnauthorizedException(
    reason: String? = null,
    cause: Throwable? = null
) : Exception(reason, cause)