package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.core.user.SessionManagementRepository
import com.cocot3ro.gh.almacen.core.user.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.UserRoleDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class ManageAlmacenItemUseCase(
    @Provided private val networkRepository: NetworkRepository,
    @Provided private val sessionManagementRepository: SessionManagementRepository
) {

    fun getAll(): Flow<List<AlmacenItemDomain>> = networkRepository.getAlmacenItems().map { list ->
        list.map(AlmacenItemModel::toDomain)
    }

    suspend fun create(item: AlmacenItemDomain, imageData: Pair<ByteArray, String>?) {
        if (sessionManagementRepository.getUser()?.toDomain()?.role != UserRoleDomain.ADMIN)
            return

        networkRepository.createAlmacenItem(item, imageData)
    }

    suspend fun takeStock(item: AlmacenItemDomain, amount: Int) {
        networkRepository.almacenItemTakeStock(item, amount)
    }

    fun addStock(item: AlmacenItemDomain, amount: Int) {
        networkRepository.almacenItemAddStock(item, amount)
    }

    suspend fun edit(item: AlmacenItemDomain, imageData: Pair<ByteArray, String>?) {
        if (sessionManagementRepository.getUser()?.toDomain()?.role != UserRoleDomain.ADMIN)
            return

        networkRepository.editAlamcenItem(item, imageData)
    }

    suspend fun delete(item: AlmacenItemDomain) {
        if (sessionManagementRepository.getUser()?.toDomain()?.role != UserRoleDomain.ADMIN)
            return

        networkRepository.deleteAlmacenItem(item)
    }
}