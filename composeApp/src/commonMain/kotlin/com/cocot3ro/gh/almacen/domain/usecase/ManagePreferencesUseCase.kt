package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.core.datastore.DatastoreRepository
import com.cocot3ro.gh.almacen.core.datastore.model.AuthPreferenceCore
import com.cocot3ro.gh.almacen.core.datastore.model.PreferenceCore
import com.cocot3ro.gh.almacen.core.datastore.model.ext.toDomain
import com.cocot3ro.gh.almacen.domain.model.AuthPreferenceItem
import com.cocot3ro.gh.almacen.domain.model.PreferenceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class ManagePreferencesUseCase(
    @Provided private val datastoreRepository: DatastoreRepository,
) {

    fun getPreferences(): Flow<PreferenceItem> = datastoreRepository.getPreferences()
        .map(PreferenceCore::toDomain)

    suspend fun setHost(host: String) {
        datastoreRepository.setHost(host)
    }

    suspend fun setPort(port: UShort) {
        datastoreRepository.setPort(port)
    }

    fun getAuthPreferences(): Flow<AuthPreferenceItem> = datastoreRepository.getAuthPreferences()
        .map(AuthPreferenceCore::toDomain)

    suspend fun setJwtToken(token: String?) {
        datastoreRepository.setJwtToken(token)
    }

    suspend fun setRefreshToken(token: String?) {
        datastoreRepository.setRefreshToken(token)
    }
}