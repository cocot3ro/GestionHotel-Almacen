package com.cocot3ro.gh.almacen.domain.state.ext

import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.ex.BadRequestException
import com.cocot3ro.gh.almacen.domain.state.ex.ForbiddenException
import com.cocot3ro.gh.almacen.domain.state.ex.NotFoundException
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import com.cocot3ro.gh.almacen.domain.state.ex.UnexpectedResponseException

fun ResponseState.isSuccess(): Boolean {
    return this is ResponseState.OK<*> ||
            this is ResponseState.NoContent ||
            this is ResponseState.Created<*>
}

fun ResponseState.getExceptionOrNull(): Throwable? {
    return when (this) {
        is ResponseState.OK<*>,
        ResponseState.NoContent,
        ResponseState.PartialContent,
        is ResponseState.Created<*> -> null

        ResponseState.NotFound -> NotFoundException()
        ResponseState.Forbidden -> ForbiddenException()
        ResponseState.BadRequest -> BadRequestException()
        ResponseState.Unauthorized -> UnauthorizedException()

        is ResponseState.Error -> this.cause
    }
}

fun ResponseState.getExceptionOrDefault(): Throwable {
    return getExceptionOrNull() ?: UnexpectedResponseException()
}

fun ResponseState.getExceptionOrElse(def: Throwable): Throwable {
    return getExceptionOrNull() ?: def
}