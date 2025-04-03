package com.cocot3ro.gh.almacen

import com.cocot3ro.gh.almacen.data.database.AlmacenDbRepository
import com.cocot3ro.gh.almacen.data.database.toModel
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenResource
import com.cocot3ro.gh.almacen.data.network.toDatabase
import com.cocot3ro.gh.model.network.data.almacen.AlmacenModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.resources.Resources
import io.ktor.server.resources.delete
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(SSE)
    install(Resources)

    val almacenDbRepository by inject<AlmacenDbRepository>()

    routing {
        sse(path = AlmacenResource.All().getRoute()) {
            almacenDbRepository.getAllAsFlow()
                .map { list -> list.map(AlmacenEntity::toModel) }
                .map(Json.Default::encodeToString)
                .flowOn(Dispatchers.IO)
                .collect(::send)
        }

        post<AlmacenResource> {
            val almacenModel = call.receive<AlmacenModel>().copy(id = 0)
            val ent = almacenModel.toDatabase()
            val id = almacenDbRepository.insert(ent)
            call.respond(HttpStatusCode.Created, ent.copy(id = id).toModel())
        }

        put<AlmacenResource.Id.Edit> {
            val editId = it.parent.id

            val dbEntity = almacenDbRepository.getById(editId)
            val fromBody = call.receive<AlmacenModel>().toDatabase()

            dbEntity?.let {
                if (dbEntity == fromBody) {
                    call.response.status(value = HttpStatusCode.NoContent)
                    return@put
                }

                val updateEntity = dbEntity.copy(
                    name = fromBody.name,
                    quantity = fromBody.quantity,
                    packSize = fromBody.packSize,
                    minimum = fromBody.minimum
                )

                almacenDbRepository.update(updateEntity)
                call.respond(status = HttpStatusCode.OK, message = updateEntity.toModel())
            } ?: run {
                val newId = almacenDbRepository.insert(fromBody)
                call.respond(
                    status = HttpStatusCode.Created,
                    message = fromBody.copy(id = newId).toModel()
                )
            }
        }

        delete<AlmacenResource.Id> {
            val deleteId = it.id
            val dbEntity = almacenDbRepository.getById(deleteId)

            dbEntity?.let {
                almacenDbRepository.delete(deleteId)
                call.response.status(value = HttpStatusCode.NoContent)
            } ?: run {
                call.response.status(value = HttpStatusCode.NotFound)
            }
        }
    }
}
