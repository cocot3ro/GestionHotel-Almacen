package com.cocot3ro.gh.almacen.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.cocot3ro.gh.almacen.data.database.AlmacenDbRepository
import com.cocot3ro.gh.almacen.data.database.DatabaseConstants
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.nio.file.Path
import kotlin.io.path.absolutePathString

val databaseModule = module {
    singleOf(::AlmacenDbRepository) {
        createdAtStart()
    }

    single<JdbcSqliteDriver> {
        val os = System.getProperty("os.name").lowercase()
        val localDir = when {
            os.startsWith("win", ignoreCase = false) -> {
                Path.of(System.getProperty("LOCALAPPDATA"))
            }

            else -> {
                Path.of(
                    System.getProperty("user.home"),
                    ".local",
                    "shared"
                )
            }
        }.absolutePathString()

        val dbPath = Path.of(localDir, "Gestion Hotel", "GH Almacen")
        dbPath.toFile().let { dbFile ->
            if (!dbFile.exists()) {
                dbFile.mkdirs()
            }
        }

        JdbcSqliteDriver(
            url = "jdbc:sqlite:${
                Path.of(
                    dbPath.absolutePathString(),
                    DatabaseConstants.DATABASE_FILE_NAME
                )
            }"
        )
    }
}
