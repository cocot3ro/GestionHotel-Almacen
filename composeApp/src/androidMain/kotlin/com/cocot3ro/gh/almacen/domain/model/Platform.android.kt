package com.cocot3ro.gh.almacen.domain.model

class AndroidPlatform : Platform {
    override val appDistribution: AppDistribution
        get() = AppDistribution.ANDROID
}

actual fun getPlatform(): Platform = AndroidPlatform()