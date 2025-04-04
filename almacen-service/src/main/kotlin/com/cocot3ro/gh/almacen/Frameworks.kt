package com.cocot3ro.gh.almacen

import com.cocot3ro.gh.almacen.di.databaseModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(databaseModule)
    }
}
