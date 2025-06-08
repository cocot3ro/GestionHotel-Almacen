package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class TestConnectionUseCase(
    @Provided private val networkRepository: NetworkRepository
) {
    operator fun invoke(host: String, port: UShort): Flow<ResponseState> {
        return networkRepository.testConnection(host, port)
    }
}
