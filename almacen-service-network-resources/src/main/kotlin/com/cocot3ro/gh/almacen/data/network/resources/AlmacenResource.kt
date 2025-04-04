package com.cocot3ro.gh.almacen.data.network.resources

import com.cocot3ro.gh.core.ResourceRoute
import io.ktor.resources.Resource

@Resource(AlmacenResource.PATH)
class AlmacenResource : ResourceRoute {

    companion object {
        const val PATH: String = "/api/almacen"
    }

    override fun getRoute(): String = PATH

    @Resource(All.PATH)
    data class All(
        val parent: AlmacenResource = AlmacenResource(),
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "all"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }

    @Resource(Id.PATH)
    data class Id(
        val parent: AlmacenResource = AlmacenResource(),
        val id: Long,
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "{id}"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$id"

        @Resource(Edit.PATH)
        class Edit(
            val parent: Id,
        ) : ResourceRoute {

            companion object {
                const val PATH: String = "edit"
            }

            override fun getRoute(): String = "${parent.getRoute()}/$PATH"
        }
    }
}