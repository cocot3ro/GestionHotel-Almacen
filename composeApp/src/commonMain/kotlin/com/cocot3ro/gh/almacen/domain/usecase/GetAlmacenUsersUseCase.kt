package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class GetAlmacenUsersUseCase(
    @Provided private val networkRepository: NetworkRepository,
) {
    operator fun invoke(): Flow<List<AlmacenUserDomain>> = networkRepository.getAlmacenUsers()
        .map { list ->
            list.map(AlmacenUserModel::toDomain)
        }
}
