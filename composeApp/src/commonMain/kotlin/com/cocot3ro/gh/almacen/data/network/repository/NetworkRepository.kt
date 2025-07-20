package com.cocot3ro.gh.almacen.data.network.repository

import com.cocot3ro.gh.almacen.data.network.NetworkConstants
import com.cocot3ro.gh.almacen.data.network.client.GhAlmacenClient
import com.cocot3ro.gh.almacen.data.network.client.UpdateClient
import com.cocot3ro.gh.almacen.data.network.model.AlmacenAddStockModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenTakeMultipleStockModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenTakeStockModel
import com.cocot3ro.gh.almacen.data.network.model.AppVersionModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.model.ext.toModel
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.ex.NotFoundException
import com.cocot3ro.gh.almacen.domain.state.ex.UnexpectedResponseException
import com.cocot3ro.gh.services.login.LoginRequestModel
import com.cocot3ro.gh.services.login.LoginResponseModel
import com.cocot3ro.gh.services.login.RefreshRequestModel
import com.cocot3ro.gh.services.users.UserModel
import com.cocot3ro.gh.services.users.UserPasswordChangeModel
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates

class NetworkRepository(
    private val client: GhAlmacenClient,
    private val updateClient: UpdateClient
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

    fun testConnection(host: String, port: UShort): Flow<ResponseState> = flow {
        val response: HttpResponse = client.testConnection(host, port)

        when (response.status) {
            HttpStatusCode.OK -> emit(ResponseState.OK(Unit))
            else -> emit(
                ResponseState.Error(
                    cause = UnexpectedResponseException(reason = "Unexpected response: ${response.status}"),
                )
            )
        }
    }

    fun login(user: UserDomain, password: String?): Flow<ResponseState> = flow {
        val request = LoginRequestModel(user.toModel().id, password)
        val response: HttpResponse = client.login(request)

        when (response.status) {
            HttpStatusCode.OK -> {
                val result: LoginResponseModel = response.body<LoginResponseModel>()
                emit(ResponseState.OK(result.toDomain()))
            }

            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException(reason = "Unexpected response: ${response.status}")))
            }
        }
    }

    fun refresh(token: String): Flow<ResponseState> = flow {
        val response: HttpResponse = client.refresh(RefreshRequestModel(token))
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.OK -> {
                val result: LoginResponseModel = response.body<LoginResponseModel>()
                emit(ResponseState.OK(result.toDomain()))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException(reason = "Unexpected response: ${response.status}")))
            }
        }
    }

    fun getUsers(): Flow<ResponseState> = callbackFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.wsUsers()
        with(webSocketSession) {
            try {
                while (true) {
                    val items: List<UserModel> =
                        receiveDeserialized<List<UserModel>>().map { item ->
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
                    send(ResponseState.OK(items))
                }
            } catch (e: Exception) {
                send(ResponseState.Error(UnexpectedResponseException(cause = e)))
                close(e)
            }
        }
    }

    fun createUser(
        user: UserDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> = flow {
        val multipart: List<PartData> = formData {
            append("data", Json.encodeToString(user.toModel()))
            imageData?.let { (image: ByteArray, imageName: String) ->
                append(
                    key = "image",
                    value = image,
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentType,
                            ContentType.Image.Any.toString()
                        )
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${imageName}\""
                        )
                    }
                )
            }
        }

        val response: HttpResponse = client.postUser(multipart)

        when (response.status) {
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.Created -> {
                emit(ResponseState.Created(response.body<UserModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun editUser(
        user: UserDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> = flow {

        val model: UserModel = user.toModel()

        val multipart: List<PartData> = formData {
            append("data", Json.encodeToString(model))
            imageData?.let { (image: ByteArray, imageName: String) ->
                append(
                    key = "image",
                    value = image,
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentType,
                            ContentType.Image.Any.toString()
                        )
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${imageName}\""
                        )
                    }
                )
            }
        }


        val response: HttpResponse = client.putUser(
            id = model.id,
            multipart = multipart
        )
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<UserModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun changeUserPassword(
        user: UserDomain,
        currPass: String,
        newPass: String
    ): Flow<ResponseState> = flow {
        val response: HttpResponse = client.patchUser(
            user.toModel().id,
            UserPasswordChangeModel(currPass, newPass)
        )

        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<UserModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun deleteUser(user: UserDomain): Flow<ResponseState> = flow {
        val response: HttpResponse = client.deleteUser(user.toModel())
        when (response.status) {
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.NoContent -> emit(ResponseState.NoContent)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun getAlmacenStores(): Flow<ResponseState> = callbackFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.wsAlmacenStores()
        with(webSocketSession) {
            try {
                while (true) {
                    val items: List<AlmacenStoreModel> = receiveDeserialized()
                    send(ResponseState.OK(items))
                }
            } catch (e: Exception) {
                send(ResponseState.Error(UnexpectedResponseException(cause = e)))
                close(e)
            }
        }
    }

    fun createAlmacenStore(store: AlmacenStoreDomain): Flow<ResponseState> = flow {
        val response: HttpResponse = client.postAlmacenStore(store.toModel())
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)

            HttpStatusCode.Created -> {
                emit(ResponseState.Created(response.body<AlmacenStoreModel>()))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun editAlmacenStore(store: AlmacenStoreDomain): Flow<ResponseState> = flow {
        val response: HttpResponse = client.putAlmacenStore(store.toModel())
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<AlmacenStoreModel>()))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun deleteAlmacenStore(store: AlmacenStoreDomain): Flow<ResponseState> = flow {
        val response: HttpResponse = client.deleteAlmacenStore(store.toModel())
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.NoContent -> emit(ResponseState.NoContent)

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun getAlmacenItems(): Flow<ResponseState> = channelFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.wsAlmacenItems()
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

                    send(ResponseState.OK(items))
                }
            } catch (e: Exception) {
                send(ResponseState.Error(UnexpectedResponseException(cause = e)))
                close(e)
            }
        }
    }

    fun createAlmacenItem(
        item: AlmacenItemDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> = flow {
        val multipart: List<PartData> = formData {
            append("data", Json.encodeToString(item.toModel()))
            imageData?.let { (image: ByteArray, imageName: String) ->
                append(
                    key = "image",
                    value = image,
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentType,
                            ContentType.Image.Any.toString()
                        )
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${imageName}\""
                        )
                    }
                )
            }
        }

        val response: HttpResponse = client.postAlmacenItem(multipart)
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)

            HttpStatusCode.Created -> {
                emit(ResponseState.Created(response.body<AlmacenItemModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun editAlmacenItem(
        item: AlmacenItemDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> = flow {
        val model: AlmacenItemModel = item.toModel()

        val multipart: List<PartData> = formData {
            append("data", Json.encodeToString(model))
            imageData?.let { (image: ByteArray, imageName: String) ->
                append(
                    key = "image",
                    value = image,
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentType,
                            ContentType.Image.Any.toString()
                        )
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${imageName}\""
                        )
                    }
                )
            }
        }

        val response: HttpResponse = client.putAlmacenItem(model.id, multipart)
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<AlmacenItemModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun almacenItemAddStock(
        item: AlmacenItemDomain,
        amount: Int
    ): Flow<ResponseState> = flow {
        val response: HttpResponse = client.postAddStock(
            item.toModel(),
            AlmacenAddStockModel(quantity = amount)
        )
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<AlmacenItemModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun almacenItemAddMultipleStock(
        items: Map<AlmacenItemDomain, Int>
    ): Flow<ResponseState> = flow {
        val response: HttpResponse =
            client.postAddMultipleStock(items.mapKeys { (k: AlmacenItemDomain, _) -> k.toModel().id })

        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.PartialContent -> emit(ResponseState.PartialContent)
            HttpStatusCode.NoContent -> emit(ResponseState.NoContent)

            HttpStatusCode.OK -> emit(ResponseState.OK(Unit))

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun almacenItemTakeStock(
        item: AlmacenItemDomain,
        amount: Int,
        store: AlmacenStoreDomain
    ): Flow<ResponseState> = flow {
        val response: HttpResponse = client.postTakeStock(
            item.toModel(),
            AlmacenTakeStockModel(storeId = store.toModel().id, quantity = amount)
        )
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<AlmacenItemModel>().let { responseItem ->
                    responseItem.copy(image = responseItem.image?.let {
                        "%s://%s:%d$it".format(
                            NetworkConstants.SCHEME,
                            host,
                            port.toInt()
                        )
                    })
                }))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun almacenItemTakeMultipleStock(
        items: Map<AlmacenItemDomain, Int>,
        store: AlmacenStoreDomain
    ): Flow<ResponseState> = flow {
        val response: HttpResponse =
            client.postTakeMultipleStock(
                AlmacenTakeMultipleStockModel(
                    storeId = store.toModel().id,
                    map = items.mapKeys { (k: AlmacenItemDomain, _) -> k.toModel().id }
                )
            )

        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.PartialContent -> emit(ResponseState.PartialContent)
            HttpStatusCode.NoContent -> emit(ResponseState.NoContent)

            HttpStatusCode.OK -> emit(ResponseState.OK(Unit))

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    fun deleteAlmacenItem(item: AlmacenItemDomain): Flow<ResponseState> = flow {
        val response: HttpResponse = client.deleteAlmacenItem(item.toModel())
        when (response.status) {
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.NoContent -> emit(ResponseState.NoContent)

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException("Unexpected response: ${response.status}")))
            }
        }
    }

    suspend fun getLatestVersion(): Result<AppVersionModel> {
        val response: HttpResponse = runCatching { updateClient.getLatestVersion() }
            .onFailure { return Result.failure(it) }
            .getOrThrow()

        return when (response.status) {
            HttpStatusCode.OK -> Result.success(response.body<AppVersionModel>())
            HttpStatusCode.NotFound -> Result.failure(NotFoundException("Latest version not found"))
            else -> Result.failure(
                UnexpectedResponseException(
                    "Unexpected response: ${response.status}"
                )
            )
        }
    }
}
