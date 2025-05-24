package com.cocot3ro.gh.almacen.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.cocot3ro.gh.almacen.core.StorageConstants
import com.cocot3ro.gh.almacen.data.database.AlmacenDbRepository
import com.cocot3ro.gh.almacen.data.database.DatabaseConstants
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.nio.file.Path

val databaseModule = module {
    singleOf(::AlmacenDbRepository) {
        createdAtStart()
    }

    single<JdbcSqliteDriver> {
        JdbcSqliteDriver(
            url = "jdbc:sqlite:${
                Path.of(
                    StorageConstants.databaseDir,
                    DatabaseConstants.DATABASE_FILE_NAME
                )
            }"
        )
    }
}
