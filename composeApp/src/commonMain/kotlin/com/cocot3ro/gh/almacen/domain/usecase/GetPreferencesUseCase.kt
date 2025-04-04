package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.core.datastore.DatastoreRepository
import com.cocot3ro.gh.almacen.domain.model.PreferenceItem
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class GetPreferencesUseCase(
    @Provided private val datastoreRepository: DatastoreRepository,
) {

    operator fun invoke(): Flow<PreferenceItem> = datastoreRepository.getPreferences()

}