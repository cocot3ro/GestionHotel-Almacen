package com.cocot3ro.gh.almacen.data.database

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.cocot3ro.gh.almacen.AlmacenDatabase
import com.cocot3ro.gh.almacen.AlmacenItemEntity
import com.cocot3ro.gh.almacen.AlmacenItemEntityQueries
import com.cocot3ro.gh.almacen.AlmacenStoreEntity
import com.cocot3ro.gh.almacen.AlmacenStoreEntityQueries
import com.cocot3ro.gh.almacen.AlmacenUserEntity
import com.cocot3ro.gh.almacen.AlmacenUserEntityQueries
import com.cocot3ro.gh.almacen.data.database.adapter.LongArrayColumnAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class AlmacenDbRepository(
    driver: JdbcSqliteDriver
) {
    private val database: AlmacenDatabase
    private val almacenItemDao: AlmacenItemEntityQueries
    private val almacenUserDao: AlmacenUserEntityQueries
    private val almacenStoreDao: AlmacenStoreEntityQueries

    init {
        AlmacenDatabase.Schema.create(driver)
        database = AlmacenDatabase(
            driver = driver,
            almacenItemEntityAdapter = AlmacenItemEntity.Adapter(
                quantityAdapter = IntColumnAdapter,
                packSizeAdapter = IntColumnAdapter,
                minimumAdapter = IntColumnAdapter,
                barcodesAdapter = LongArrayColumnAdapter
            ),
            almacenUserEntityAdapter = AlmacenUserEntity.Adapter(
                roleAdapter = EnumColumnAdapter()
            )
        )

        almacenItemDao = database.almacenItemEntityQueries
        almacenUserDao = database.almacenUserEntityQueries
        almacenStoreDao = database.almacenStoreEntityQueries
    }

    fun getAlmacenItems(): Flow<List<AlmacenItemEntity>> =
        almacenItemDao.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getAlmacenItemById(id: Long): AlmacenItemEntity? =
        almacenItemDao.selectById(id).executeAsOneOrNull()

    fun insertAlmacenItem(almacenItemEntity: AlmacenItemEntity): Long {
        return database.transactionWithResult {
            val (_, barcodes, name, supplier, image, quantity, packSize, minimum) = almacenItemEntity
            almacenItemDao.insert(barcodes, name, supplier, image, quantity, packSize, minimum)
            almacenItemDao.lastInsertRowId().executeAsOne()
        }
    }

    fun updateAlmacenItem(almacenItemEntity: AlmacenItemEntity) {
        almacenItemDao.update(
            id = almacenItemEntity.id,
            barcodes = almacenItemEntity.barcodes,
            name = almacenItemEntity.name,
            supplier = almacenItemEntity.supplier,
            image = almacenItemEntity.image,
            quantity = almacenItemEntity.quantity,
            packSize = almacenItemEntity.packSize,
            minimum = almacenItemEntity.minimum
        )
    }

    fun deleteAlmacenItem(id: Long) {
        almacenItemDao.delete(id)
    }

    fun getAlmacenUsers(): Flow<List<AlmacenUserEntity>> =
        almacenUserDao.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getAlmacenUserById(id: Long): AlmacenUserEntity? =
        almacenUserDao.selectById(id).executeAsOneOrNull()

    fun insertAlmacenUser(almacenUserEntity: AlmacenUserEntity): Long {
        return database.transactionWithResult {
            val (_, name, image, passwordHash, isAdmin) = almacenUserEntity
            almacenUserDao.insert(name, image, passwordHash, isAdmin)
            almacenUserDao.lastInsertRowId().executeAsOne()
        }
    }

    fun updateAlmacenUser(almacenUserEntity: AlmacenUserEntity) {
        almacenUserDao.update(
            id = almacenUserEntity.id,
            name = almacenUserEntity.name,
            image = almacenUserEntity.image
        )
    }

    fun deleteAlmacenUser(id: Long) {
        almacenUserDao.delete(id)
    }

    fun getAlmacenStores(): Flow<List<AlmacenStoreEntity>> =
        almacenStoreDao.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    fun getAlmacenStoreById(id: Long): AlmacenStoreEntity? =
        almacenStoreDao.selectById(id).executeAsOneOrNull()

    fun insertAlmacenStore(almacenStoreEntity: AlmacenStoreEntity): Long {
        return database.transactionWithResult {
            val (_, name, image) = almacenStoreEntity
            almacenStoreDao.insert(name, image)
            almacenStoreDao.lastInsertRowId().executeAsOne()
        }
    }

    fun updateAlmacenStore(almacenStoreEntity: AlmacenStoreEntity) {
        almacenStoreDao.update(
            id = almacenStoreEntity.id,
            name = almacenStoreEntity.name,
            image = almacenStoreEntity.image
        )
    }

    fun deleteAlmacenStore(id: Long) {
        almacenStoreDao.delete(id)
    }
}
