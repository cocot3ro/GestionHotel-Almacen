package com.cocot3ro.gh.almacen

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val tmpConfigFile: File = Paths.get(
        System.getProperty("java.io.tmpdir"),
        "almacen-service.conf"
    ).toFile()

    if (developmentMode && !tmpConfigFile.exists()) {
        this::class.java.getResourceAsStream("/application.conf")!!.use { input ->
            tmpConfigFile.also(File::createNewFile).outputStream().use { bw ->
                input.copyTo(bw)
            }
        }
    }

    configureFrameworks()
    configureSerialization()
    configureRouting()
}
