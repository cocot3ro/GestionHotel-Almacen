package com.cocot3ro.gh.almacen.data.network.client

import com.cocot3ro.gh.almacen.data.network.resources.UpdatesResource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.get
import io.ktor.client.statement.HttpResponse

class UpdateClient(
    private val client: HttpClient
) {
    suspend fun getLatestVersion(): HttpResponse {
        return client.get(resource = UpdatesResource())
    }
}