package com.cocot3ro.gh.almacen.data.network.resources

import com.cocot3ro.gh.core.ResourceRoute
import io.ktor.resources.Resource

@Resource(AlmacenLoginRequestResource.PATH)
class AlmacenLoginRequestResource : ResourceRoute {

    companion object {
        const val PATH: String = "/api/almacen/auth"
    }

    override fun getRoute(): String = PATH

    @Resource(Login.PATH)
    data class Login(
        val parent: AlmacenLoginRequestResource = AlmacenLoginRequestResource()
    ) : ResourceRoute {
        companion object {
            const val PATH: String = "login"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }

    @Resource(Logout.PATH)
    data class Logout(
        val parent: AlmacenLoginRequestResource = AlmacenLoginRequestResource()
    ) : ResourceRoute {
        companion object {
            const val PATH: String = "logout"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }

    @Resource(Refresh.PATH)
    data class Refresh(
        val parent: AlmacenLoginRequestResource = AlmacenLoginRequestResource()
    ) : ResourceRoute {
        companion object {
            const val PATH: String = "refresh"
        }

        override fun getRoute(): String = "${parent.getRoute()}/$PATH"
    }
}