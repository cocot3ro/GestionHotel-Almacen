package com.cocot3ro.gh.almacen.data.network.resources

import com.cocot3ro.gh.core.ResourceRoute
import io.ktor.resources.Resource

@Resource(path = AlmacenUserResource.PATH)
class AlmacenUserResource : ResourceRoute {

    companion object {
        const val PATH: String = "/api/almacen/users"
    }

    override fun getRoute(): String = PATH

    @Resource(path = All.PATH)
    data class All(
        val parent: AlmacenUserResource = AlmacenUserResource(),
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "all"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }

    @Resource(path = Id.PATH)
    data class Id(
        val parent: AlmacenUserResource = AlmacenUserResource(),
        val id: Long,
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "{id}"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$id"
    }
}
