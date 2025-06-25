package com.cocot3ro.gh.almacen.di

import com.cocot3ro.gh.almacen.data.network.client.GhAlmacenClient
import com.cocot3ro.gh.almacen.data.network.client.UpdateClient
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.model.AlmacenLoginResponseDomain
import com.cocot3ro.gh.almacen.domain.model.AuthPreferenceItem
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule: Module = module {
    single<GhAlmacenClient> {
        GhAlmacenClient(client = get<HttpClient>(named("basicClient")))
    }

    single<UpdateClient> {
        UpdateClient(client = get<HttpClient>(named("updateClient")))
    }

    singleOf(::NetworkRepository)

    single<HttpClient>(named("updateClient")) {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }

            install(Resources)

            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "cocot3ro.github.io"
                }
            }
        }
    }

    single<HttpClient>(named("basicClient")) {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }

            install(Resources)
        }
    }

    factory<HttpClient>(named("authClient")) {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json()
            }

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }

            install(Resources)

            install(Auth) {
                bearer {
                    val managePreferencesUseCase: ManagePreferencesUseCase by inject()
                    val manageLoginUsecase: ManageLoginUsecase by inject()

                    loadTokens {
                        val authPrefs: AuthPreferenceItem = managePreferencesUseCase
                            .getAuthPreferences().first()

                        authPrefs
                            .takeIf {
                                it.jwtToken != null && it.refreshToken != null
                            }
                            ?.let {
                                BearerTokens(
                                    accessToken = it.jwtToken!!,
                                    refreshToken = it.refreshToken!!
                                )
                            }
                    }

                    refreshTokens {
                        runCatching<BearerTokens?> {
                            oldTokens?.let { oldTokens ->
                                val newTokens = manageLoginUsecase.refresh(oldTokens.refreshToken!!)
                                    .first()

                                (newTokens as? ResponseState.OK<*>)
                                    ?.let { newTokens.data as AlmacenLoginResponseDomain }
                                    ?.let { response: AlmacenLoginResponseDomain ->
                                        managePreferencesUseCase.setJwtToken(response.jwtToken)
                                        managePreferencesUseCase.setRefreshToken(response.refreshToken)

                                        BearerTokens(
                                            accessToken = response.jwtToken,
                                            refreshToken = response.refreshToken
                                        )
                                    }
                            }
                        }.getOrNull()
                    }
                }
            }
        }
    }
}
