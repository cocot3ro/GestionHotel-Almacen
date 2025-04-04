package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.core.datastore.DatastoreRepository
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class SavePreferencesUseCase(
    @Provided private val datastoreRepository: DatastoreRepository,
) {

    suspend fun setHost(host: String) {
        datastoreRepository.setHost(host)
    }

    suspend fun setPort(port: Int) {
        datastoreRepository.setPort(port)
    }

    suspend fun setFirstTime(firstTime: Boolean) {
        datastoreRepository.setFirstTime(firstTime)
    }

}