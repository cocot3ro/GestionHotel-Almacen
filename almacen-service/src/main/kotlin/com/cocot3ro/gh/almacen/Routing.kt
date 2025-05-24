package com.cocot3ro.gh.almacen

import com.cocot3ro.gh.almacen.core.StorageConstants
import com.cocot3ro.gh.almacen.data.database.AlmacenDbRepository
import com.cocot3ro.gh.almacen.data.database.ext.toModel
import com.cocot3ro.gh.almacen.data.database.model.UserRoleEntity
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenLoginRequestModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStockModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenStoreModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserModel
import com.cocot3ro.gh.almacen.data.network.model.AlmacenUserPasswordChangeModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDatabase
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenImageResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenItemResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenLoginRequestResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenStoreResource
import com.cocot3ro.gh.almacen.data.network.resources.AlmacenUserResource
import com.password4j.Password
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticFiles
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.resources.Resources
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.utils.io.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.io.File

fun Application.configureRouting() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    install(Resources)

    almacenStatus()
    almacenImagesRouting()
    almacenItemsRouting()
    almacenUsersRouting()
    almacenStoresRouting()
    almacenLoginGateway()
}

private fun Application.almacenStatus() {
    routing {
        get<AlmacenResource.Status> { _ ->
            call.response.status(value = HttpStatusCode.OK)
        }
    }
}

private fun Application.almacenLoginGateway() {
    val almacenDbRepository: AlmacenDbRepository by inject()

    routing {
        post<AlmacenLoginRequestResource> { _ ->
            val loginRequest: AlmacenLoginRequestModel = call.receive<AlmacenLoginRequestModel>()

            val dbUser: AlmacenUserEntity? =
                almacenDbRepository.getAlmacenUserById(loginRequest.userId)

            if (dbUser == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "Invalid user or password"
                )
                return@post
            }

            when (dbUser.role) {
                UserRoleEntity.ADMIN -> {

                    if (loginRequest.password == null) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Missing password"
                        )
                        return@post
                    }

                    if (!Password.check(loginRequest.password!!, dbUser.passwordHash!!)
                            .withArgon2()
                    ) {
                        call.respond(
                            status = HttpStatusCode.Unauthorized,
                            message = "Invalid user or password"
                        )
                        return@post
                    } else {
                        call.response.status(HttpStatusCode.OK)
                    }
                }

                UserRoleEntity.USER -> {
                    call.response.status(HttpStatusCode.OK)
                }
            }
        }
    }
}

private fun Application.almacenImagesRouting() {
    routing {
        staticFiles(
            remotePath = AlmacenImageResource.Item().getRoute(),
            dir = File(StorageConstants.almacenItemImagesDir),
            index = null
        )

        staticFiles(
            remotePath = AlmacenImageResource.User().getRoute(),
            dir = File(StorageConstants.almacenUserImagesDir),
            index = null
        )

        staticFiles(
            remotePath = AlmacenImageResource.Store().getRoute(),
            dir = File(StorageConstants.almacenStoreImagesDir),
            index = null
        )
    }
}

private fun Application.almacenItemsRouting() {

    val almacenDbRepository: AlmacenDbRepository by inject()

    routing {
        webSocket(path = AlmacenItemResource.All().getRoute()) {
            almacenDbRepository.getAlmacenItems()
                .map { list -> list.map(AlmacenItemEntity::toModel) }
                .flowOn(Dispatchers.IO)
                .collect(::sendSerialized)
        }

        post<AlmacenItemResource> { _ ->
            var itemModel: AlmacenItemModel? = null
            var imagePair: Pair<ByteArray, String>? = null

            val multipart: MultiPartData = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "data" -> {
                                itemModel = Json.decodeFromString<AlmacenItemModel>(part.value)
                                    .copy(id = 0L, image = null)
                            }
                        }
                    }

                    is PartData.FileItem -> if (part.name == "image") {
                        imagePair = part.provider().toByteArray() to part.originalFileName!!
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (itemModel == null) {
                call.respond(HttpStatusCode.BadRequest, message = "Missing item data")
                return@post
            }

            val entity: AlmacenItemEntity = itemModel!!.toDatabase()
            val id: Long = almacenDbRepository.insertAlmacenItem(entity)

            val createdEntity: AlmacenItemEntity = almacenDbRepository.getAlmacenItemById(id)!!

            if (imagePair == null) {
                call.respond(
                    HttpStatusCode.Created,
                    createdEntity.toModel()
                )

                return@post
            }

            val (imageBytes, originalFileName) = imagePair!!

            val imageFileName = originalFileName.substringAfterLast('.', "png").let { extension ->
                "${id}_${createdEntity.name}_${System.currentTimeMillis()}.$extension"
            }

            File(StorageConstants.almacenItemImagesDir, imageFileName)
                .also(File::createNewFile)
                .writeBytes(imageBytes)

            val savedImageUrl: String = AlmacenImageResource.Item.Id(id = imageFileName)
                .getRoute()

            almacenDbRepository.updateAlmacenItem(createdEntity.copy(image = savedImageUrl))

            call.respond(
                status = HttpStatusCode.Created,
                message = almacenDbRepository.getAlmacenItemById(id)!!.toModel()
            )
        }

        put<AlmacenItemResource.Id> { idRes ->
            val dbEntity: AlmacenItemEntity? = almacenDbRepository.getAlmacenItemById(idRes.id)

            if (dbEntity == null) {
                call.respond(HttpStatusCode.NotFound, "Item with id ${idRes.id} not found")
                return@put
            }

            var updatedModel: AlmacenItemModel? = null
            var imageFileData: Pair<ByteArray, String>? = null

            val multipart: MultiPartData = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "data") {
                        updatedModel = Json.decodeFromString<AlmacenItemModel>(part.value)
                    }

                    is PartData.FileItem -> if (part.name == "image") {
                        imageFileData = part.provider().toByteArray() to part.originalFileName!!
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (updatedModel == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing item data")
                return@put
            }

            var imageUrl: String? = dbEntity.image

            when {
                imageFileData != null -> {
                    // Replace the current image with the new one
                    dbEntity.image?.let { oldImageUrl ->
                        File(
                            StorageConstants.almacenItemImagesDir,
                            oldImageUrl.substringAfterLast('/')
                        ).takeIf(File::exists)
                            ?.let(File::delete)
                    }

                    val (bytes, fileName) = imageFileData!!
                    val extension = fileName.substringAfterLast('.', "png")
                    val newImageFileName =
                        "${idRes.id}_${updatedModel!!.name}_${System.currentTimeMillis()}.$extension"

                    File(StorageConstants.almacenItemImagesDir, newImageFileName)
                        .also(File::createNewFile)
                        .writeBytes(bytes)

                    imageUrl = AlmacenImageResource.Item.Id(id = newImageFileName).getRoute()
                }

                updatedModel!!.image == null && dbEntity.image != null -> {
                    // If `image` is null, delete the current image (if any)
                    File(
                        StorageConstants.almacenItemImagesDir,
                        dbEntity.image.substringAfterLast('/')
                    ).takeIf(File::exists)
                        ?.let(File::delete)
                    imageUrl = null
                }

                // else -> keep current image
            }

            val updatedEntity = dbEntity.copy(
                name = updatedModel!!.name,
                quantity = updatedModel!!.quantity,
                packSize = updatedModel!!.packSize,
                minimum = updatedModel!!.minimum,
                image = imageUrl,
                supplier = updatedModel!!.supplier,
                barcodes = updatedModel!!.barcodes
            )

            almacenDbRepository.updateAlmacenItem(updatedEntity)
            call.respond(HttpStatusCode.OK, updatedEntity.toModel())
        }

        post<AlmacenItemResource.Id.TakeStock> { res ->
            val dbEntity: AlmacenItemEntity? = almacenDbRepository.getAlmacenItemById(res.parent.id)

            dbEntity?.let {
                val quantity: Int = call.receive<AlmacenStockModel>().quantity
                val newQuantity: Int = (dbEntity.quantity.toLong() - quantity.toLong())
                    .coerceAtLeast(0L).toInt()

                val newEntity = dbEntity.copy(quantity = newQuantity)
                almacenDbRepository.updateAlmacenItem(newEntity)
                call.respond(status = HttpStatusCode.OK, message = newEntity)
            } ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = "Item with id ${res.parent.id} not found"
                )
            }
        }

        post<AlmacenItemResource.Id.AddStock> { res ->
            val dbEntity: AlmacenItemEntity? = almacenDbRepository.getAlmacenItemById(res.parent.id)

            dbEntity?.let {
                val quantity: Int = call.receive<AlmacenStockModel>().quantity

                // Prevent int overflow
                val newQuantity: Int = (dbEntity.quantity.toLong() + quantity.toLong())
                    .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

                val newEntity = dbEntity.copy(quantity = newQuantity)

                almacenDbRepository.updateAlmacenItem(newEntity)
                call.respond(status = HttpStatusCode.OK, message = newEntity.toModel())
            } ?: run {
                call.respond(
                    status = HttpStatusCode.NotFound,
                    message = "Item with id ${res.parent.id} not found"
                )
            }
        }

        delete<AlmacenItemResource.Id> { idRes ->
            val dbEntity: AlmacenItemEntity? = almacenDbRepository.getAlmacenItemById(idRes.id)

            dbEntity?.let {
                almacenDbRepository.deleteAlmacenItem(idRes.id)
                call.response.status(value = HttpStatusCode.NoContent)
            } ?: run {
                call.response.status(value = HttpStatusCode.NotFound)
            }
        }
    }
}

private fun Application.almacenUsersRouting() {

    val almacenDbRepository: AlmacenDbRepository by inject()

    routing {
        get<AlmacenUserResource.All> { _ ->
            almacenDbRepository.getAlmacenUsers()
                .map { list -> list.map(AlmacenUserEntity::toModel) }
                .flowOn(Dispatchers.IO)
                .collect {
                    call.respond(status = HttpStatusCode.OK, message = it)
                }
        }

        post<AlmacenUserResource> { _ ->
            var userModel: AlmacenUserModel? = null
            var imagePair: Pair<ByteArray, String>? = null
            var hash: String? = null

            val multipart: MultiPartData = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> when (part.name) {
                        "data" -> {
                            userModel = Json.decodeFromString<AlmacenUserModel>(part.value)
                                .copy(id = 0L, image = null)
                        }

                        "password" -> {
                            hash = part.value.takeUnless(String::isBlank)?.let {
                                Password.hash(it).withArgon2().result
                            }
                        }
                    }

                    is PartData.FileItem -> if (part.name == "image") {
                        imagePair = part.provider().toByteArray() to part.originalFileName!!
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (userModel == null) {
                call.respond(HttpStatusCode.BadRequest, message = "Missing user data")
                return@post
            }

            val entity: AlmacenUserEntity = userModel!!.toDatabase().copy(passwordHash = hash)
            val id: Long = almacenDbRepository.insertAlmacenUser(entity)

            val createdEntity: AlmacenUserEntity = almacenDbRepository.getAlmacenUserById(id)!!

            if (imagePair == null) {
                call.respond(
                    HttpStatusCode.Created,
                    createdEntity.toModel()
                )

                return@post
            }

            val (imageBytes: ByteArray, originalFileName: String) = imagePair!!

            val imageFileName: String = originalFileName.substringAfterLast(
                delimiter = '.',
                missingDelimiterValue = "png"
            ).let { extension ->
                "${id}_${createdEntity.name}_${System.currentTimeMillis()}.$extension"
            }

            File(StorageConstants.almacenUserImagesDir, imageFileName)
                .also(File::createNewFile)
                .writeBytes(imageBytes)

            val savedImageUrl: String = AlmacenImageResource.User.Id(id = imageFileName)
                .getRoute()

            almacenDbRepository.updateAlmacenUser(createdEntity.copy(image = savedImageUrl))

            call.respond(
                status = HttpStatusCode.Created,
                message = almacenDbRepository.getAlmacenUserById(id)!!.toModel()
            )
        }

        put<AlmacenUserResource.Id> { idRes ->
            val dbEntity: AlmacenUserEntity = almacenDbRepository.getAlmacenUserById(idRes.id)
                ?: run {
                    call.respond(HttpStatusCode.NotFound, "User with id ${idRes.id} not found")
                    return@put
                }

            var updatedModel: AlmacenUserModel? = null
            var imageFileData: Pair<ByteArray, String>? = null

            val multipart: MultiPartData = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "data") {
                        updatedModel = Json.decodeFromString<AlmacenUserModel>(part.value)
                    }

                    is PartData.FileItem -> if (part.name == "image") {
                        imageFileData = part.provider().toByteArray() to part.originalFileName!!
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (updatedModel == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing user data")
                return@put
            }

            var imageUrl: String? = dbEntity.image

            when {
                // Add a new image
                imageFileData != null -> {
                    dbEntity.image?.let { oldImageUrl ->
                        File(
                            StorageConstants.almacenUserImagesDir,
                            oldImageUrl.substringAfterLast('/')
                        ).takeIf(File::exists)
                            ?.let(File::delete)
                    }

                    val (bytes: ByteArray, fileName: String) = imageFileData!!
                    val extension = fileName.substringAfterLast('.', "png")
                    val newImageFileName =
                        "${idRes.id}_${updatedModel!!.name}_${System.currentTimeMillis()}.$extension"

                    File(StorageConstants.almacenUserImagesDir, newImageFileName)
                        .also(File::createNewFile)
                        .writeBytes(bytes)

                    imageUrl = AlmacenImageResource.User.Id(id = newImageFileName).getRoute()
                }

                // Delete the current image
                updatedModel!!.image == null && dbEntity.image != null -> {
                    // If `image` is null, delete the current image (if any)
                    File(
                        StorageConstants.almacenUserImagesDir,
                        dbEntity.image.substringAfterLast('/')
                    ).takeIf(File::exists)
                        ?.let(File::delete)
                    imageUrl = null
                }

                // else -> keep current image
            }

            val updatedEntity: AlmacenUserEntity = dbEntity.copy(
                name = updatedModel!!.name,
                image = imageUrl,
                role = updatedModel!!.role.toDatabase()
            )

            almacenDbRepository.updateAlmacenUser(updatedEntity)
            call.respond(HttpStatusCode.OK, updatedEntity.toModel())
        }

        patch<AlmacenUserResource.Id> { idRes ->
            val pwsdModel: AlmacenUserPasswordChangeModel = call.receive()
            var dbEntity: AlmacenUserEntity = almacenDbRepository.getAlmacenUserById(idRes.id)
                ?: run {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        message = "User with id ${idRes.id} not found"
                    )
                    return@patch
                }

            if (dbEntity.passwordHash == null) {
                if (pwsdModel.newPassword == null) return@patch

                dbEntity = dbEntity.copy(
                    passwordHash = Password.hash(pwsdModel.newPassword).withArgon2().result
                )
                almacenDbRepository.updateAlmacenUser(dbEntity)
                call.respond(status = HttpStatusCode.OK, message = dbEntity.toModel())
                return@patch
            }

            if (!Password.check(pwsdModel.currentPassword, dbEntity.passwordHash).withArgon2()) {
                call.respond(status = HttpStatusCode.Unauthorized, message = "Invalid password")
                return@patch
            }

            dbEntity = dbEntity.copy(
                // If new password is null, remove the password
                // else, hash the new password and save it
                passwordHash = pwsdModel.newPassword?.let { Password.hash(it).withArgon2().result }
            )

            almacenDbRepository.updateAlmacenUser(dbEntity)
            call.respond(status = HttpStatusCode.OK, message = dbEntity.toModel())
        }

        delete<AlmacenUserResource.Id> { idRes ->
            val dbEntity: AlmacenUserEntity? = almacenDbRepository.getAlmacenUserById(idRes.id)

            dbEntity?.let { _ ->
                almacenDbRepository.deleteAlmacenUser(dbEntity.id)
                call.response.status(value = HttpStatusCode.NoContent)
            } ?: run {
                call.response.status(value = HttpStatusCode.NotFound)
            }
        }
    }
}

private fun Application.almacenStoresRouting() {

    val almacenDbRepository: AlmacenDbRepository by inject()

    routing {
        get<AlmacenStoreResource.All> { _ ->
            almacenDbRepository.getAlmacenStores()
                .map { list -> list.map(AlmacenStoreEntity::toModel) }
                .flowOn(Dispatchers.IO)
                .collect {
                    call.respond(status = HttpStatusCode.OK, message = it)
                }
        }

        post<AlmacenStoreResource> { _ ->
            var storeModel: AlmacenStoreModel? = null
            var imagePair: Pair<ByteArray, String>? = null

            val multipart: MultiPartData = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "data") {
                        storeModel = Json.decodeFromString<AlmacenStoreModel>(part.value)
                            .copy(id = 0L, image = null)
                    }

                    is PartData.FileItem -> if (part.name == "image") {
                        imagePair = part.provider().toByteArray() to part.originalFileName!!
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (storeModel == null) {
                call.respond(HttpStatusCode.BadRequest, message = "Missing store data")
                return@post
            }

            val entity: AlmacenStoreEntity = storeModel!!.toDatabase()
            val id: Long = almacenDbRepository.insertAlmacenStore(entity)

            val createdEntity: AlmacenStoreEntity = almacenDbRepository.getAlmacenStoreById(id)!!

            if (imagePair == null) {
                call.respond(
                    HttpStatusCode.Created,
                    createdEntity.toModel()
                )

                return@post
            }

            val (imageBytes: ByteArray, originalFileName: String) = imagePair!!

            val imageFileName: String = originalFileName.substringAfterLast(
                delimiter = '.',
                missingDelimiterValue = "png"
            ).let { extension ->
                "${id}_${createdEntity.name}_${System.currentTimeMillis()}.$extension"
            }

            File(StorageConstants.almacenStoreImagesDir, imageFileName)
                .also(File::createNewFile)
                .writeBytes(imageBytes)

            val savedImageUrl: String = AlmacenImageResource.Store.Id(id = imageFileName)
                .getRoute()

            almacenDbRepository.updateAlmacenStore(createdEntity.copy(image = savedImageUrl))

            call.respond(
                status = HttpStatusCode.Created,
                message = almacenDbRepository.getAlmacenStoreById(id)!!.toModel()
            )
        }

        put<AlmacenStoreResource.Id> { idRes ->
            val dbEntity: AlmacenStoreEntity? = almacenDbRepository.getAlmacenStoreById(idRes.id)

            if (dbEntity == null) {
                call.respond(HttpStatusCode.NotFound, "Store with id ${idRes.id} not found")
                return@put
            }

            var updatedModel: AlmacenStoreModel? = null
            var imageFileData: Pair<ByteArray, String>? = null

            val multipart: MultiPartData = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> if (part.name == "data") {
                        updatedModel = Json.decodeFromString<AlmacenStoreModel>(part.value)
                    }

                    is PartData.FileItem -> if (part.name == "image") {
                        imageFileData = part.provider().toByteArray() to part.originalFileName!!
                    }

                    else -> Unit
                }
                part.dispose()
            }

            if (updatedModel == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing store data")
                return@put
            }

            var imageUrl: String? = dbEntity.image

            when {
                // Add a new image
                imageFileData != null -> {
                    dbEntity.image?.let { oldImageUrl ->
                        File(
                            StorageConstants.almacenStoreImagesDir,
                            oldImageUrl.substringAfterLast('/')
                        ).takeIf(File::exists)
                            ?.let(File::delete)
                    }

                    val (bytes: ByteArray, fileName: String) = imageFileData!!
                    val extension: String = fileName.substringAfterLast(
                        delimiter = '.',
                        missingDelimiterValue = "png"
                    )
                    val newImageFileName =
                        "${idRes.id}_${updatedModel!!.name}_${System.currentTimeMillis()}.$extension"

                    File(StorageConstants.almacenStoreImagesDir, newImageFileName)
                        .also(File::createNewFile)
                        .writeBytes(bytes)

                    imageUrl = AlmacenImageResource.Store.Id(id = newImageFileName).getRoute()
                }

                // Delete the current image
                updatedModel!!.image == null && dbEntity.image != null -> {
                    // If `image` is null, delete the current image (if any)
                    File(
                        StorageConstants.almacenStoreImagesDir,
                        dbEntity.image.substringAfterLast('/')
                    ).takeIf(File::exists)
                        ?.let(File::delete)
                    imageUrl = null
                }
            }

            val updatedEntity = dbEntity.copy(
                name = updatedModel!!.name,
                image = imageUrl
            )

            almacenDbRepository.updateAlmacenStore(updatedEntity)
            call.respond(HttpStatusCode.OK, updatedEntity.toModel())
        }

        delete<AlmacenStoreResource.Id> { idRes ->
            val dbEntity: AlmacenStoreEntity? =
                almacenDbRepository.getAlmacenStoreById(idRes.id)

            dbEntity?.let {
                almacenDbRepository.deleteAlmacenStore(dbEntity.id)
                call.response.status(value = HttpStatusCode.NoContent)
            } ?: run {
                call.response.status(value = HttpStatusCode.NotFound)
            }
        }
    }
}
