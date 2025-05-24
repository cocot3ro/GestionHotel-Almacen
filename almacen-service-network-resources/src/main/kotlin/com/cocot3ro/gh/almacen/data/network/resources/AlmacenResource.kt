package com.cocot3ro.gh.almacen.data.network.resources

import com.cocot3ro.gh.core.ResourceRoute
import io.ktor.resources.Resource

@Resource(path = AlmacenResource.PATH)
class AlmacenResource : ResourceRoute {

    companion object {
        const val PATH: String = "/api/almacen"
    }

    override fun getRoute(): String = PATH

    @Resource(path = Status.PATH)
    data class Status(
        val parent: AlmacenResource = AlmacenResource(),
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "status"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }

}