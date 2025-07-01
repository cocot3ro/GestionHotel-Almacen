package com.cocot3ro.gh.almacen.core.user

import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.model.ext.toCore
import org.koin.core.annotation.Single

@Single
class SessionManagementRepository {

    private var loggedUser: UserCore? = null

    fun setUser(user: UserDomain?) {
        loggedUser = user?.toCore()
    }

    fun getUser(): UserCore? {
        return loggedUser?.copy()
    }
}
