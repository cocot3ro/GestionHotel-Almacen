package com.cocot3ro.gh.almacen

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.cocot3ro.gh.almacen.data.database.AlmacenDbRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.nio.file.Path

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(databaseModule())
    }
}

private fun Application.databaseModule() = module {
    singleOf(::AlmacenDbRepository) {
        createdAtStart()
    }

    single<JdbcSqliteDriver> {
        val dbPath = environment.config.property("almacen-service.db.path").getString()
        val dbName = environment.config.property("almacen-service.db.name").getString()

        val dbDir = Path.of(dbPath).toFile()

        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        JdbcSqliteDriver(url = "jdbc:sqlite:${Path.of(dbPath, dbName)}")
    }
}
