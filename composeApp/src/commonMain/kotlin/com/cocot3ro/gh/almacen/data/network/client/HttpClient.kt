package com.cocot3ro.gh.almacen.data.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
fun provideHttpClient(
    @InjectedParam host: String,
    @InjectedParam port: Int,
): HttpClient {
    return HttpClient(OkHttp) {
        defaultRequest {
            this.host = host
            this.port = port
        }

        install(ContentNegotiation) {
            json()
        }

        install(SSE)
        install(Resources)
    }
}
