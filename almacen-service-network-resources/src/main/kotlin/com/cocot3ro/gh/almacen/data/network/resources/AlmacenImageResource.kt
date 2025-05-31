package com.cocot3ro.gh.almacen.data.network.resources

import com.cocot3ro.gh.core.ResourceRoute
import io.ktor.resources.Resource

@Resource(AlmacenImageResource.PATH)
class AlmacenImageResource : ResourceRoute {

    companion object {
        const val PATH: String = "/api/almacen/images"
    }

    override fun getRoute(): String = PATH

    @Resource(User.PATH)
    data class User(
        val parent: AlmacenImageResource = AlmacenImageResource()
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "user"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"

        @Resource(Id.PATH)
        data class Id(
            val parent: User = User(),
            val id: String
        ) : ResourceRoute {

            companion object {
                const val PATH: String = "{id}"
            }

            override fun getRoute(): String = "${parent.getRoute()}/$id"
        }
    }

    @Resource(Item.PATH)
    data class Item(
        val parent: AlmacenImageResource = AlmacenImageResource()
    ) : ResourceRoute {

        companion object {
            const val PATH: String = "item"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"


        @Resource(Id.PATH)
        data class Id(
            val parent: Item = Item(),
            val id: String
        ) : ResourceRoute {

            companion object {
                const val PATH: String = "{id}"
            }

            override fun getRoute(): String = "${parent.getRoute()}/$id"
        }

    }
}