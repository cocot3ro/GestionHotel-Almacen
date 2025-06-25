package com.cocot3ro.gh.almacen.domain.model

interface Platform {
    val appDistribution: AppDistribution
}

expect fun getPlatform(): Platform