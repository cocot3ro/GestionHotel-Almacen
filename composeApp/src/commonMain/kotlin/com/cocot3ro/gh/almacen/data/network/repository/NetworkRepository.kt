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

    fun getAlmacenUsers(): Flow<List<AlmacenUserModel>> = flow {
        val response: HttpResponse = client.getAlmacenUsers()
        val users: List<AlmacenUserModel> = response.body<List<AlmacenUserModel>>().map { user ->
            user.copy(
                image = user.image?.let {
                    "%s://%s:%d$it".format(
                        NetworkConstants.SCHEME,
                        host,
                        port.toInt()
                    )
                }
            )
        }

        emit(users)
    }

    suspend fun createAlmacenUser(user: AlmacenUserDomain) {
        client.postAlmacenUser(user.toModel())
    }

    suspend fun editAlmacenUser(user: AlmacenUserDomain) {
        client.putAlmacenUser(user.toModel())
    }

    suspend fun deleteAlmacenUser(user: AlmacenUserDomain) {
        client.deleteAlmacenUser(user.toModel())
    }

    fun getAlmacenStores(): Flow<List<AlmacenStoreModel>> = flow {
        val response = client.getAlmacenStores()
        val stores = response.body<List<AlmacenStoreModel>>().map { store ->
            store.copy(
                image = store.image?.let {
                    "%s://%s:%d$it".format(
                        NetworkConstants.SCHEME,
                        host,
                        port.toInt()
                    )
                }
            )
        }

        emit(stores)
    }

    suspend fun createAlmacenStore(store: AlmacenStoreDomain) {
        client.postAlmacenStore(store.toModel())
    }

    suspend fun editAlmacenStore(store: AlmacenStoreDomain) {
        client.putAlmacenStore(store.toModel())
    }

    suspend fun deleteAlmacenStore(store: AlmacenStoreDomain) {
        client.deleteAlmacenStore(store.toModel())
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

    suspend fun createAlmacenItem(item: AlmacenItemDomain, image: ByteArray?, imageName: String?) {
        client.postAlmacenItem(item.toModel(), image, imageName)
    }

    suspend fun editAlamcenItem(item: AlmacenItemDomain, image: ByteArray?, imageName: String?) {
        client.putAlmacenItem(item.toModel(), image, imageName)
    }

    suspend fun almacenItemAddStock(item: AlmacenItemDomain, amount: Int) {
        client.postAddStock(item.toModel(), AlmacenStockModel(amount))
    }

    suspend fun almacenItemTakeStock(item: AlmacenItemDomain, amount: Int) {
        client.postTakeStock(item.toModel(), AlmacenStockModel(amount))
    }

    suspend fun deleteAlmacenItem(item: AlmacenItemDomain) {
        client.deleteAlmacenItem(item.toModel())
    }
}
