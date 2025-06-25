package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class SetUpConnectionValuesUseCase(
    @Provided private val networkRepository: NetworkRepository
) {
    operator fun invoke(host: String, port: UShort) {
        networkRepository.initConnectionValues(
            host = host,
            port = port
        )
    }
}