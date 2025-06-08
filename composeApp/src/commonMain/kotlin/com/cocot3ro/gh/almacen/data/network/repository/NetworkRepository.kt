package com.cocot3ro.gh.almacen.data.network.repository

import com.cocot3ro.gh.almacen.data.network.NetworkConstants
import com.cocot3ro.gh.almacen.data.network.client.GhAlmacenClient
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginRequestModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginResponseModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStockModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserPasswordChangeModel
import com.cocot3ro.gh.almacen.data.network.model.RefreshTokenRequestModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.domain.model.ext.toModel
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.ex.UnexpectedResponseException
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

    fun login(user: AlmacenUserDomain, password: String?): Flow<ResponseState> = flow {
        val request = AlmacenLoginRequestModel(user.toModel().id, password)
        val response: HttpResponse = client.login(request)

        when (response.status) {
            HttpStatusCode.OK -> {
                val result: AlmacenLoginResponseModel = response.body<AlmacenLoginResponseModel>()
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
        val response: HttpResponse = client.refresh(RefreshTokenRequestModel(token))
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.OK -> {
                val result: AlmacenLoginResponseModel = response.body<AlmacenLoginResponseModel>()
                emit(ResponseState.OK(result.toDomain()))
            }

            else -> {
                emit(ResponseState.Error(UnexpectedResponseException(reason = "Unexpected response: ${response.status}")))
            }
        }
    }

    fun getAlmacenUsers(): Flow<ResponseState> = callbackFlow {
        val webSocketSession: DefaultClientWebSocketSession = client.wsAlmacenUsers()
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
                    send(ResponseState.OK(items))
                }
            } catch (e: Exception) {
                send(ResponseState.Error(UnexpectedResponseException(cause = e)))
                close(e)
            }
        }
    }

    fun createAlmacenUser(
        user: AlmacenUserDomain,
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

        val response: HttpResponse = client.postAlmacenUser(multipart)

        when (response.status) {
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)

            HttpStatusCode.Created -> {
                emit(ResponseState.Created(response.body<AlmacenUserModel>().let { responseItem ->
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

    fun editAlmacenUser(
        user: AlmacenUserDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> = flow {

        val model: AlmacenUserModel = user.toModel()

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


        val response: HttpResponse = client.putAlmacenUser(
            id = model.id,
            multipart = multipart
        )
        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<AlmacenUserModel>().let { responseItem ->
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

    fun changeAlmacenUserPassword(
        user: AlmacenUserDomain,
        currPass: String,
        newPass: String
    ): Flow<ResponseState> = flow {
        val response: HttpResponse = client.patchAlmacenUser(
            user.toModel().id,
            AlmacenUserPasswordChangeModel(currPass, newPass)
        )

        when (response.status) {
            HttpStatusCode.Unauthorized -> emit(ResponseState.Unauthorized)
            HttpStatusCode.NotFound -> emit(ResponseState.NotFound)
            HttpStatusCode.BadRequest -> emit(ResponseState.BadRequest)
            HttpStatusCode.Forbidden -> emit(ResponseState.Forbidden)

            HttpStatusCode.OK -> {
                emit(ResponseState.OK(response.body<AlmacenUserModel>().let { responseItem ->
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

    fun deleteAlmacenUser(user: AlmacenUserDomain): Flow<ResponseState> = flow {
        val response: HttpResponse = client.deleteAlmacenUser(user.toModel())
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

    fun editAlamcenItem(
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

    fun almacenItemAddStock(item: AlmacenItemDomain, amount: Int): Flow<ResponseState> = flow {
        val response: HttpResponse = client.postAddStock(item.toModel(), AlmacenStockModel(amount))
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

    fun almacenItemTakeStock(item: AlmacenItemDomain, amount: Int): Flow<ResponseState> = flow {
        val response: HttpResponse = client.postTakeStock(item.toModel(), AlmacenStockModel(amount))
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
}
