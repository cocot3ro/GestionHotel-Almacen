package com.cocot3ro.gh.almacen.domain.state.ex

class UnexpectedResponseException(
    reason: String? = null,
    cause: Throwable? = null
) : Exception(reason, cause)