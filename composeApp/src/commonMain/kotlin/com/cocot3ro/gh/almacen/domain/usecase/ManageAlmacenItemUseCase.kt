package com.cocot3ro.gh.almacen.domain.usecase

import com.cocot3ro.gh.almacen.data.network.model.AlmacenItemModel
import com.cocot3ro.gh.almacen.data.network.model.ext.toDomain
import com.cocot3ro.gh.almacen.data.network.repository.NetworkRepository
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single
class ManageAlmacenItemUseCase(
    @Provided private val networkRepository: NetworkRepository
) {

    fun getAll(): Flow<ResponseState> {
        return networkRepository.getAlmacenItems().map { response: ResponseState ->
            if (response !is ResponseState.OK<*>) return@map response

            @Suppress("UNCHECKED_CAST")
            return@map ResponseState.OK(
                (response.data as List<AlmacenItemModel>)
                    .map(AlmacenItemModel::toDomain)
            )
        }
    }

    fun create(
        item: AlmacenItemDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> {
        return networkRepository.createAlmacenItem(item, imageData).map { response: ResponseState ->
            if (response !is ResponseState.Created<*>) return@map response

            return@map ResponseState.Created((response.data as AlmacenItemModel).toDomain())
        }
    }

    fun takeStock(item: AlmacenItemDomain, amount: Int): Flow<ResponseState> {
        return networkRepository.almacenItemTakeStock(item, amount).map { response: ResponseState ->
            if (response !is ResponseState.OK<*>) return@map response

            return@map ResponseState.OK((response.data as AlmacenItemModel).toDomain())
        }
    }

    fun addStock(item: AlmacenItemDomain, amount: Int): Flow<ResponseState> {
        return networkRepository.almacenItemAddStock(item, amount).map { response: ResponseState ->
            if (response !is ResponseState.OK<*>) return@map response

            return@map ResponseState.OK((response.data as AlmacenItemModel).toDomain())
        }
    }

    fun edit(
        item: AlmacenItemDomain,
        imageData: Pair<ByteArray, String>?
    ): Flow<ResponseState> {
        return networkRepository.editAlmacenItem(item, imageData).map { response: ResponseState ->
            if (response !is ResponseState.OK<*>) return@map response

            return@map ResponseState.OK((response.data as AlmacenItemModel).toDomain())
        }
    }

    fun delete(item: AlmacenItemDomain): Flow<ResponseState> {
        return networkRepository.deleteAlmacenItem(item)
    }
}