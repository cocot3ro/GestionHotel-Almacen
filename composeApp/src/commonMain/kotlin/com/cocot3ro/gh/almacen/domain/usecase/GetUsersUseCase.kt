package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.services.users.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class GetUsersUseCase(
    @Provided private val networkRepository: NetworkRepository,
) {
    operator fun invoke(): Flow<ResponseState> {
        return networkRepository.getUsers().map { response: ResponseState ->
            if (response !is ResponseState.OK<*>) return@map response

            @Suppress("UNCHECKED_CAST")
            return@map ResponseState.OK(
                (response.data as List<UserModel>)
                    .map(UserModel::toDomain)
            )
        }
    }
}
