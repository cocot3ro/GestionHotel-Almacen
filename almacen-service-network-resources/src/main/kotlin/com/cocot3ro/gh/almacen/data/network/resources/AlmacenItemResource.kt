package com.cocot3ro.gh.almacen.data.network.resources

import com.cocot3ro.gh.core.ResourceRoute
import io.ktor.resources.Resource

@Resource(AlmacenItemResource.PATH)
class AlmacenItemResource : ResourceRoute {

    companion object {
        const val PATH: String = "/api/almacen/items"
    }

    override fun getRoute(): String = PATH

    @Resource(All.PATH)
    data class All(
        val parent: AlmacenItemResource = AlmacenItemResource()
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "all"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }

    @Resource(Id.PATH)
    data class Id(
        val parent: AlmacenItemResource = AlmacenItemResource(),
        val id: Long
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "{id}"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$id"

        @Resource(TakeStock.PATH)
        data class TakeStock(
            val parent: Id
        ) : ResourceRoute {

            companion object {
                const val PATH: String = "take"
            }

            override fun getRoute(): String = "${parent.getRoute()}/$PATH"
        }

        @Resource(AddStock.PATH)
        data class AddStock(
            val parent: Id
        ) : ResourceRoute {

            companion object {
                const val PATH: String = "add"
            }

            override fun getRoute(): String = "${parent.getRoute()}/$PATH"
        }
    }
}