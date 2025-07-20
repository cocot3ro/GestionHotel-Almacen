package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.core.datastore.DatastoreRepository
import com.cocot3ro.gh.almacen.core.session.SessionManagementRepository
import com.cocot3ro.gh.almacen.core.session.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.LoginResponseDomain
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class ManageLoginUsecase(
    @Provided private val sessionManagementRepository: SessionManagementRepository,
    @Provided private val datastoreRepository: DatastoreRepository,
    @Provided private val networkRepository: NetworkRepository
) {
    fun logIn(user: UserDomain, password: String?, store: AlmacenStoreDomain): Flow<ResponseState> =
        networkRepository.login(user, password)
            .onEach { loginResult ->
                (loginResult as? ResponseState.OK<*>)
                    ?.let { it.data as LoginResponseDomain }
                    ?.let {
                        sessionManagementRepository.setUser(user)
                        datastoreRepository.setJwtToken(it.jwtToken)
                        datastoreRepository.setRefreshToken(it.refreshToken)

                        sessionManagementRepository.setStore(store)
                    }
            }
            .catch { throwable: Throwable ->
                emit(ResponseState.Error(throwable))
            }

    suspend fun logOut() {
        sessionManagementRepository.setUser(null)
        sessionManagementRepository.setStore(null)
        datastoreRepository.setJwtToken(null)
        datastoreRepository.setRefreshToken(null)
    }

    fun getLoggedUser(): UserDomain? {
        return sessionManagementRepository.getUser()?.toDomain()
    }

    fun getLoggedStore(): AlmacenStoreDomain? {
        return sessionManagementRepository.getStore()?.toDomain()
    }

    fun refresh(accessToken: String): Flow<ResponseState> {
        return networkRepository.refresh(accessToken)
    }
}