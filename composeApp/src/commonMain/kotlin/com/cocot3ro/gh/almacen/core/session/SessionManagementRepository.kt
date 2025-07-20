package com.cocot3ro.gh.almacen.core.session

import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.model.ext.toCore
import org.koin.core.annotation.Single

@Single
class SessionManagementRepository {

    private var loggedUser: UserCore? = null
    private var loggedStore: AlmacenStoreCore? = null

    fun setUser(user: UserDomain?) {
        loggedUser = user?.toCore()
    }

    fun getUser(): UserCore? {
        return loggedUser?.copy()
    }

    fun setStore(store: AlmacenStoreDomain?) {
        loggedStore = store?.toCore()
    }

    fun getStore(): AlmacenStoreCore? {
        return loggedStore?.copy()
    }
}
