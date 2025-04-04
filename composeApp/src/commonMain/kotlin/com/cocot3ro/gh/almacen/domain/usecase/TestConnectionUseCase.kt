package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.core.CoreResource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

@Single
class TestConnectionUseCase {

    operator fun invoke(host: String, port: Int): Flow<Boolean> {
        val client: HttpClient by inject(clazz = HttpClient::class.java) {
            parametersOf(host, port)
        }

        return flow {
            runCatching { client.get(CoreResource.Status()) }
                .onFailure { throwable ->
                    throw throwable
                }
                .onSuccess { response ->
                    emit(response.status == HttpStatusCode.OK)
                }
        }
    }

}
