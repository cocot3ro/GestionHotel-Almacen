package com.cocot3ro.gh.almacen.data.network.repository

import com.cocot3ro.gh.almacen.data.network.NetworkConstants
import com.cocot3ro.gh.almacen.data.network.client.GhAlmacenClient
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginRequestModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginResponseModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStockModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.data.network.model.RefreshTokenRequestModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.domain.model.ext.toModel
import com.cocot3ro.gh.almacen.domain.state.LoginResult
import com.cocot3ro.gh.almacen.domain.state.TestConnectionResult
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlin.properties.Delegates

class NetworkRepository(
    private val client: GhAlmacenClient
) {
    private lateinit var host: String
    private var port: UShort by Delegates.notNull()

    fun initConnectionValues(host: String, port: UShort) {
        this.host = host
        this.port = port

        client.initConnectionValues(
            host = host,
            port = port
        )
    }

    fun testConnection(host: String, port: UShort): Flow<TestConnectionResult> = flow {
        val response: HttpResponse = client.testConnection(host, port)

        emit(
            when (response.status) {
                HttpStatusCode.OK -> TestConnectionResult.Success
                HttpStatusCode.ServiceUnavailable -> TestConnectionResult.ServiceUnavailable
                else -> TestConnectionResult.Error(
                    cause = Exception("Unexpected response: ${response.status}"),
                )
            }
        )
    }

    fun login(user: AlmacenUserDomain, password: String?): Flow<LoginResult> = flow {
        val request = AlmacenLoginRequestModel(user.id, password)
        val response: HttpResponse = client.login(request)

        when (response.status) {
            HttpStatusCode.OK -> {
                val result: AlmacenLoginResponseModel = response.body<AlmacenLoginResponseModel>()
                emit(LoginResult.Success(result.toDomain()))
            }

            HttpStatusCode.Unauthorized -> emit(LoginResult.Unauthorized)
            else -> emit(LoginResult.Error(Exception("Unexpected response: ${response.status}")))
        }
    }

    fun refresh(token: String): Flow<LoginResult> = flow {
        val response: HttpResponse = client.refresh(RefreshTokenRequestModel(token))
        when (response.status) {
            HttpStatusCode.OK -> {
                val result: AlmacenLoginResponseModel = response.body<AlmacenLoginResponseModel>()
                emit(LoginResult.Success(result.toDomain()))
            }

            HttpStatusCode.Unauthorized -> emit(LoginResult.Unauthorized)

            else -> emit(LoginResult.Error(Exception("Unexpected response: ${response.status}")))
        }
    }

    fun getAlmacenUsers(): Flow<List<AlmacenUserModel>> = callbackFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.getAlmacenUsers()
        with(webSocketSession) {
            try {
                while (true) {
                    val items: List<AlmacenUserModel> =
                        receiveDeserialized<List<AlmacenUserModel>>().map { item ->
                            item.copy(
                                image = item.image?.let {
                                    "%s://%s:%d$it".format(
                                        NetworkConstants.SCHEME,
                                        host,
                                        port.toInt()
                                    )
                                }
                            )
                        }
                    send(items)
                }
            } catch (e: Exception) {
                close(e)
            }
        }
    }

    fun createAlmacenUser(user: AlmacenUserDomain, imageData: Pair<ByteArray, String>?) = flow {
        val response: HttpResponse = client.postAlmacenUser(user.toModel(), imageData)

        emit(TODO())
    }

    fun editAlmacenUser(user: AlmacenUserDomain, imageData: Pair<ByteArray, String>?) = flow {
        val response: HttpResponse = client.putAlmacenUser(user.toModel(), imageData)

        emit(TODO())
    }

    fun deleteAlmacenUser(user: AlmacenUserDomain) = flow {
        val response: HttpResponse = client.deleteAlmacenUser(user.toModel())

        emit(TODO())
    }

    fun getAlmacenStores(): Flow<List<AlmacenStoreModel>> = callbackFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.getAlmacenStores()
        with(webSocketSession) {
            try {
                while (true) {
                    val items: List<AlmacenStoreModel> = receiveDeserialized()
                    send(items)
                }
            } catch (e: Exception) {
                close(e)
            }
        }
    }

    fun createAlmacenStore(store: AlmacenStoreDomain) = flow {
        val response: HttpResponse = client.postAlmacenStore(store.toModel())

        emit(TODO())
    }

    fun editAlmacenStore(store: AlmacenStoreDomain) = flow {
        val response: HttpResponse = client.putAlmacenStore(store.toModel())

        emit(TODO())
    }

    fun deleteAlmacenStore(store: AlmacenStoreDomain) = flow {
        val response: HttpResponse = client.deleteAlmacenStore(store.toModel())

        emit(TODO())
    }

    fun getAlmacenItems(): Flow<List<AlmacenItemModel>> = channelFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.getAlmacenItems()
        with(webSocketSession) {
            try {
                while (true) {
                    val items: List<AlmacenItemModel> =
                        receiveDeserialized<List<AlmacenItemModel>>().map { item ->
                            item.copy(
                                image = item.image?.let {
                                    "%s://%s:%d$it".format(
                                        NetworkConstants.SCHEME,
                                        host,
                                        port.toInt()
                                    )
                                }
                            )
                        }

                    send(items)
                }
            } catch (e: Exception) {
                close(e)
            }
        }
    }

    fun createAlmacenItem(item: AlmacenItemDomain, imageData: Pair<ByteArray, String>?) = flow {
        val response: HttpResponse = client.postAlmacenItem(item.toModel(), imageData)

        emit(TODO())
    }

    fun editAlamcenItem(item: AlmacenItemDomain, imageData: Pair<ByteArray, String>?) = flow {
        val response: HttpResponse = client.putAlmacenItem(item.toModel(), imageData)

        emit(TODO())
    }

    fun almacenItemAddStock(
        item: AlmacenItemDomain,
        amount: Int
    ): Flow<List<AlmacenItemModel>> = flow {
        val response: HttpResponse = client.postAddStock(item.toModel(), AlmacenStockModel(amount))
        val body: List<AlmacenItemModel> = response.body()

        emit(body)
    }

    fun almacenItemTakeStock(item: AlmacenItemDomain, amount: Int) = flow {
        val resposne: HttpResponse = client.postTakeStock(item.toModel(), AlmacenStockModel(amount))
        val body: List<AlmacenItemModel> = resposne.body()
        emit(body)
    }

    fun deleteAlmacenItem(item: AlmacenItemDomain) = flow {
        val response: HttpResponse = client.deleteAlmacenItem(item.toModel())
        when (response.status) {

        }

        emit(TODO())
    }
}
