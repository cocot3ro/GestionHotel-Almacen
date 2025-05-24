package com.cocot3ro.gh.almacen.data.network.client

import com.cocot3ro.gh.almacen.data.network.NetworkConstants
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginRequestModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStockModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.data.network.model.RefreshTokenRequestModel
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenItemResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenLoginRequestResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenStoreResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenUserResource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.delete
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.plugins.resources.put
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject
import kotlin.properties.Delegates

class GhAlmacenClient(
    private val client: HttpClient
) {

    private val authClient: HttpClient by inject(HttpClient::class.java, named("authClient"))

    private lateinit var host: String
    private var port: UShort by Delegates.notNull()

    fun initConnectionValues(
        host: String,
        port: UShort
    ) {
        this.host = host
        this.port = port
    }

    private fun HttpRequestBuilder.setConnectionValues(): HttpRequestBuilder = apply {
        url {
            protocol = NetworkConstants.PROTOCOL
            this.host = this@GhAlmacenClient.host
            this.port = this@GhAlmacenClient.port.toInt()
        }
    }

    suspend fun testConnection(host: String, port: UShort): HttpResponse {
        return client.get(resource = AlmacenResource.Status()) {
            url {
                this.host = host
                this.port = port.toInt()
            }
        }
    }

    suspend fun login(almacenLoginRequestModel: AlmacenLoginRequestModel): HttpResponse {
        return client.post(resource = AlmacenLoginRequestResource.Login()) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(almacenLoginRequestModel)
        }
    }

    suspend fun refresh(tokenRequest: RefreshTokenRequestModel): HttpResponse {
        return authClient.post(resource = AlmacenLoginRequestResource.Refresh()) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(tokenRequest)
        }
    }

    suspend fun getAlmacenUsers(): HttpResponse {
        return client.get(resource = AlmacenUserResource.All()) {
            setConnectionValues()
        }
    }

    suspend fun postAlmacenUser(almacenModel: AlmacenUserModel): HttpResponse {
        return authClient.post(resource = AlmacenUserResource()) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(almacenModel)
        }
    }

    suspend fun putAlmacenUser(almacenModel: AlmacenUserModel): HttpResponse {
        return authClient.put(resource = AlmacenUserResource.Id(id = almacenModel.id)) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(almacenModel)
        }
    }

    suspend fun deleteAlmacenUser(almacenUserModel: AlmacenUserModel): HttpResponse {
        return authClient.delete(resource = AlmacenUserResource.Id(id = almacenUserModel.id)) {
            setConnectionValues()
        }
    }

    suspend fun getAlmacenItems(): DefaultClientWebSocketSession {
        return authClient.webSocketSession(
            host = this.host,
            port = this.port.toInt(),
            path = AlmacenItemResource.All().getRoute()
        )
    }

    suspend fun postAlmacenItem(
        almacenModel: AlmacenItemModel,
        image: ByteArray?,
        imageName: String?
    ): HttpResponse {
        return authClient.post(resource = AlmacenItemResource()) {
            setConnectionValues()

            contentType(ContentType.MultiPart.FormData)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("data", Json.encodeToString(almacenModel))
                        image?.let { image ->
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
                                        "filename=\"${imageName!!}\""
                                    )
                                }
                            )
                        }
                    }
                )
            )
        }
    }

    suspend fun putAlmacenItem(
        item: AlmacenItemModel,
        image: ByteArray?,
        imageName: String?
    ): HttpResponse {
        return authClient.put(resource = AlmacenItemResource.Id(id = item.id)) {
            setConnectionValues()

            contentType(ContentType.MultiPart.FormData)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("data", Json.encodeToString(item))
                        image?.let { image ->
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
                                        "filename=\"${imageName!!}\""
                                    )
                                }
                            )
                        }
                    }
                )
            )
        }
    }

    suspend fun postTakeStock(
        item: AlmacenItemModel,
        stockModel: AlmacenStockModel
    ): HttpResponse {
        return authClient.post(resource = AlmacenItemResource.Id.TakeStock(AlmacenItemResource.Id(id = item.id))) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(stockModel)
        }
    }

    suspend fun postAddStock(
        item: AlmacenItemModel,
        stockModel: AlmacenStockModel
    ): HttpResponse {
        return authClient.post(resource = AlmacenItemResource.Id.AddStock(AlmacenItemResource.Id(id = item.id))) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(stockModel)
        }
    }

    suspend fun deleteAlmacenItem(almacenItemModel: AlmacenItemModel): HttpResponse {
        return authClient.delete(resource = AlmacenItemResource.Id(id = almacenItemModel.id)) {
            setConnectionValues()
        }
    }

    suspend fun getAlmacenStores(): HttpResponse {
        return authClient.get(resource = AlmacenStoreResource.All()) {
            setConnectionValues()
        }
    }

    suspend fun postAlmacenStore(almacenModel: AlmacenStoreModel): HttpResponse {
        return authClient.post(resource = AlmacenStoreResource()) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(almacenModel)
        }
    }

    suspend fun putAlmacenStore(almacenModel: AlmacenStoreModel): HttpResponse {
        return authClient.put(resource = AlmacenStoreResource.Id(id = almacenModel.id)) {
            setConnectionValues()

            contentType(ContentType.Application.Json)
            setBody(almacenModel)
        }
    }

    suspend fun deleteAlmacenStore(almacenModel: AlmacenStoreModel): HttpResponse {
        return authClient.delete(resource = AlmacenStoreResource.Id(id = almacenModel.id)) {
            setConnectionValues()
        }
    }
}
