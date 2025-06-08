package com.cocot3ro.gh.almacen.domain.state.ex

class BadRequestException(
    reason: String? = null,
    cause: Throwable? = null
) : Exception(reason, cause)