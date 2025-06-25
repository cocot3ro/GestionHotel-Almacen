package com.cocot3ro.gh.almacen.core.user

import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.domain.model.ext.toCore
import org.koin.core.annotation.Single

@Single
class SessionManagementRepository {

    private var loggedUser: AlmacenUserCore? = null

    fun setUser(user: AlmacenUserDomain?) {
        loggedUser = user?.toCore()
    }

    fun getUser(): AlmacenUserCore? {
        return loggedUser?.copy()
    }
}
