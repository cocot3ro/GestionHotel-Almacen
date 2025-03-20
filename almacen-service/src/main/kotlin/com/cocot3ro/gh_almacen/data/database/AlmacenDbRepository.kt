package com.cocot3ro.gh_almacen.data.database

import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.cocot3ro.gh_almacen.AlmacenDatabase
import com.cocot3ro.ghalmacen.AlmacenEntity
import com.cocot3ro.ghalmacen.AlmacenEntityQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class AlmacenDbRepository(
    driver: JdbcSqliteDriver,
) {
    private val database: AlmacenDatabase
    private val almacenDao: AlmacenEntityQueries

    init {
        AlmacenDatabase.Schema.create(driver)
        database = AlmacenDatabase(
            driver,
            almacenEntityAdapter = AlmacenEntity.Adapter(
                quantityAdapter = IntColumnAdapter,
                packSizeAdapter = IntColumnAdapter,
                minimumAdapter = IntColumnAdapter
            )
        )
        almacenDao = database.almacenEntityQueries
    }

    fun getAllAsFlow(): Flow<List<AlmacenEntity>> =
        almacenDao.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getAll(): List<AlmacenEntity> = almacenDao.selectAll()
        .executeAsList()

    fun insert(almacenEntity: AlmacenEntity): Long {
        return database.transactionWithResult {
            val (_, name, quantity, packSize, minimum) = almacenEntity
            almacenDao.insert(name, quantity, packSize, minimum)
            almacenDao.lastInsertRowId().executeAsOne()
        }
    }

    fun getById(id: Long): AlmacenEntity? = almacenDao.selectById(id)
        .executeAsOneOrNull()

    fun update(almacenEntity: AlmacenEntity) {
        almacenDao.update(
            name = almacenEntity.name,
            quantity = almacenEntity.quantity,
            packSize = almacenEntity.packSize,
            minimum = almacenEntity.minimum,
            id = almacenEntity.id
        )
    }

    fun delete(id: Long) {
        almacenDao.delete(id)
    }
}
