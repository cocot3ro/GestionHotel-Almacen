package com.cocot3ro.gh_almacen

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform