package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.SortMode
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.ItemManagementUiState
import com.cocot3ro.gh.almacen.domain.state.ItemUiState
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.UiState
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrDefault
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrNull
import com.cocot3ro.gh.almacen.domain.state.ext.isLoadingOrReloading
import com.cocot3ro.gh.almacen.domain.usecase.ManageAlmacenItemUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import java.net.SocketException
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class AlmacenViewModel(
    @Provided private val manageLoginUsecase: ManageLoginUsecase,
    @Provided private val manageAlmacenItemUseCase: ManageAlmacenItemUseCase
) : ViewModel() {

    val loggedUser: UserDomain by lazy {
        manageLoginUsecase.getLoggedUser() ?: throw IllegalStateException("User not logged in")
    }

    val loggedStore: AlmacenStoreDomain by lazy {
        manageLoginUsecase.getLoggedStore() ?: throw IllegalStateException("Store not selected")
    }

    var showSearch: Boolean by mutableStateOf(false)
        private set
    var filter: String by mutableStateOf("")
        private set

    var showSortMode: Boolean by mutableStateOf(false)
        private set
    var sortMode: SortMode by mutableStateOf(SortMode.ID)
        private set
    var showLowStockFirst: Boolean by mutableStateOf(true)
        private set

    private val _itemManagementUiState: MutableStateFlow<ItemManagementUiState> =
        MutableStateFlow(ItemManagementUiState.Idle)
    val itemManagementUiState: StateFlow<ItemManagementUiState> =
        _itemManagementUiState.asStateFlow()

    private var fetchJob: Job? = null

    private var _itemsCache: List<AlmacenItemDomain> = emptyList()
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
        .onStart {
            _uiState.value = UiState.Loading
            fetch()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = UiState.Idle
        )

    fun refresh() {
        if (_uiState.value.isLoadingOrReloading()) return

        _uiState.value = UiState.Reloading(_itemsCache)
        fetch()
    }

    private fun onError(cause: Throwable, retry: Boolean) {
        _itemManagementUiState.value = ItemManagementUiState.Idle

        _uiState.value = UiState.Error(
            cause = cause,
            retry = retry,
            cache = _itemsCache.filter().sort()
        )
    }

    private fun fetch() {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            manageAlmacenItemUseCase.getAll()
                .retry(retries = 3) { throwable: Throwable ->
                    onError(cause = throwable, retry = throwable is SocketException)

                    throwable is SocketException
                }
                .catch { throwable: Throwable ->
                    onError(cause = throwable, retry = false)
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.Unauthorized -> {
                            onError(cause = UnauthorizedException(), retry = false)
                        }

                        is ResponseState.OK<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            _itemsCache = response.data as List<AlmacenItemDomain>

                            when (val state: ItemManagementUiState = _itemManagementUiState.value) {
                                is ItemManagementUiState.Idle -> Unit

                                is ItemManagementUiState.CreateItem -> Unit

                                is ItemManagementUiState.ToBeDeleted -> {
                                    val itemId: Long = state.item.id
                                    _itemsCache.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        if (state.state !is ItemUiState.Loading &&
                                            state.state !is ItemUiState.Success
                                        ) {
                                            _itemManagementUiState.value =
                                                ItemManagementUiState.UnexpectedDeleted(state.item)
                                        }
                                    }
                                }

                                is ItemManagementUiState.UnexpectedDeleted -> Unit

                                is ItemManagementUiState.AddStock -> {
                                    val itemId: Long = state.item.id
                                    _itemsCache.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }

                                is ItemManagementUiState.Edit -> {
                                    val itemId: Long = state.item.id
                                    _itemsCache.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }

                                is ItemManagementUiState.TakeStock -> {
                                    val itemId: Long = state.item.id
                                    _itemsCache.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }
                            }

                            _uiState.value = UiState.Success(_itemsCache.filter().sort())
                        }

                        is ResponseState.Error -> {
                            onError(cause = response.cause, retry = false)
                        }

                        else -> {
                            onError(
                                cause = response.getExceptionOrDefault(),
                                retry = false
                            )
                        }
                    }
                }
        }
    }

    private fun List<AlmacenItemDomain>.filter(): List<AlmacenItemDomain> {
        if (filter.isBlank()) return this

        return filter { item: AlmacenItemDomain ->
            item.name.contains(filter, ignoreCase = true)
        }
    }

    private fun List<AlmacenItemDomain>.sort(): List<AlmacenItemDomain> = partition {
        showLowStockFirst && it.minimum != null && it.quantity <= it.minimum
    }.let { (lowStock: List<AlmacenItemDomain>, rest: List<AlmacenItemDomain>) ->
        val sortedRest: List<AlmacenItemDomain> = when (sortMode) {
            SortMode.ID -> rest.sortedBy(AlmacenItemDomain::id)
            SortMode.NAME -> rest.sortedBy(AlmacenItemDomain::name)
            SortMode.QUANTITY -> rest.sortedBy(AlmacenItemDomain::quantity)
        }
        lowStock + sortedRest
    }

    fun updateShowSearch(showSearch: Boolean) {
        this.showSearch = showSearch
    }

    fun updateFilter(filter: String) {
        this.filter = filter
        _uiState.value = UiState.Success(_itemsCache.filter().sort())
    }

    fun updateShowSortMode(showSortMode: Boolean) {
        this.showSortMode = showSortMode
    }

    fun updateSortMode(sortMode: SortMode) {
        this.sortMode = sortMode
        _uiState.value = UiState.Success(_itemsCache.filter().sort())
    }

    fun updateShowLowStockFirst(showLowStockFirst: Boolean) {
        this.showLowStockFirst = showLowStockFirst
        _uiState.value = UiState.Success(_itemsCache.filter().sort())
    }

    fun setCreate() {
        _itemManagementUiState.value = ItemManagementUiState.CreateItem(ItemUiState.Waiting)
    }

    fun setTakeStock(item: AlmacenItemDomain) {
        _itemManagementUiState.value = ItemManagementUiState.TakeStock(item, ItemUiState.Waiting)
    }

    fun setAddStock(item: AlmacenItemDomain) {
        _itemManagementUiState.value = ItemManagementUiState.AddStock(item, ItemUiState.Waiting)
    }

    fun setEdit(item: AlmacenItemDomain) {
        _itemManagementUiState.value = ItemManagementUiState.Edit(item, ItemUiState.Waiting)
    }

    fun setDelete(item: AlmacenItemDomain) {
        _itemManagementUiState.value = ItemManagementUiState.ToBeDeleted(item, ItemUiState.Waiting)
    }

    fun clearItemManagementUiState() {
        _itemManagementUiState.value = ItemManagementUiState.Idle
    }

    fun onCreate(item: AlmacenItemDomain, imageData: Pair<ByteArray, String>?) {
        viewModelScope.launch {
            manageAlmacenItemUseCase.create(item, imageData)
                .catch { throwable: Throwable ->
                    _itemManagementUiState.apply {
                        value = (value as ItemManagementUiState.CreateItem).copy(
                            state = ItemUiState.Error(throwable)
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.Created<*> -> {
                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.CreateItem).copy(
                                    state = ItemUiState.Success
                                )
                            }
                        }

                        ResponseState.BadRequest,
                        ResponseState.Unauthorized,
                        ResponseState.Forbidden -> {
                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.CreateItem).copy(
                                    state = ItemUiState.Error(response.getExceptionOrNull()!!)
                                )
                            }
                        }

                        else -> throw IllegalStateException("Unexpected response state: $response")
                    }
                }
        }
    }

    fun onAddStock(amount: Int) {
        lateinit var item: AlmacenItemDomain

        _itemManagementUiState.apply {
            value = (value as ItemManagementUiState.AddStock).copy(state = ItemUiState.Loading)
                .also { item = it.item }
        }

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageAlmacenItemUseCase.addStock(item, amount)
                .catch { throwable: Throwable ->
                    _itemManagementUiState.apply {
                        value = (value as ItemManagementUiState.AddStock).copy(
                            state = ItemUiState.Error(throwable)
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.OK<*> -> {
                            val elapsed: Long = System.currentTimeMillis() - start

                            delay(1000 - elapsed)

                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.AddStock).copy(
                                    state = ItemUiState.Success
                                )
                            }
                        }

                        ResponseState.NotFound,
                        ResponseState.Unauthorized -> {
                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.AddStock).copy(
                                    state = ItemUiState.Error(response.getExceptionOrNull()!!)
                                )
                            }
                        }

                        else -> throw IllegalStateException("Unexpected response state: $response")
                    }
                }
        }
    }

    fun onTakeStock(amount: Int) {
        lateinit var item: AlmacenItemDomain

        _itemManagementUiState.apply {
            value = (value as ItemManagementUiState.TakeStock).copy(state = ItemUiState.Loading)
                .also { item = it.item }
        }

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageAlmacenItemUseCase.takeStock(
                item,
                amount,
                loggedStore
            )
                .catch { throwable: Throwable ->
                    _itemManagementUiState.apply {
                        value = (value as ItemManagementUiState.TakeStock).copy(
                            state = ItemUiState.Error(throwable)
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.OK<*> -> {
                            val elapsed: Long = System.currentTimeMillis() - start

                            delay(1000 - elapsed)

                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.TakeStock).copy(
                                    state = ItemUiState.Success
                                )
                            }
                        }

                        ResponseState.NotFound,
                        ResponseState.Unauthorized -> {
                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.TakeStock).copy(
                                    state = ItemUiState.Error(response.getExceptionOrNull()!!)
                                )
                            }
                        }

                        else -> throw IllegalStateException("Unexpected response state: $response")
                    }
                }
        }
    }

    fun onEdit(item: AlmacenItemDomain, imageData: Pair<ByteArray, String>?) {
        if ((_itemManagementUiState.value as ItemManagementUiState.Edit).state is ItemUiState.Loading ||
            (_itemManagementUiState.value as ItemManagementUiState.Edit).state is ItemUiState.Success
        ) {
            return
        }

        _itemManagementUiState.apply {
            value = (value as ItemManagementUiState.Edit).copy(
                item = item,
                state = ItemUiState.Loading
            )
        }

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageAlmacenItemUseCase.edit(item, imageData)
                .catch { throwable: Throwable ->
                    _itemManagementUiState.apply {
                        value = (value as ItemManagementUiState.Edit).copy(
                            state = ItemUiState.Error(throwable)
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.OK<*> -> {
                            val elapsed: Long = System.currentTimeMillis() - start

                            delay(1000 - elapsed)

                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.Edit).copy(
                                    state = ItemUiState.Success
                                )
                            }
                        }

                        ResponseState.NotFound,
                        ResponseState.Unauthorized -> {
                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.Edit).copy(
                                    state = ItemUiState.Error(response.getExceptionOrNull()!!)
                                )
                            }
                        }

                        else -> throw IllegalStateException("Unexpected response state: $response")
                    }
                }
        }
    }

    fun onDelete() {
        lateinit var item: AlmacenItemDomain

        _itemManagementUiState.apply {
            value = (value as ItemManagementUiState.ToBeDeleted).copy(state = ItemUiState.Loading)
                .also { item = it.item }
        }

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageAlmacenItemUseCase.delete(item)
                .catch { throwable: Throwable ->
                    _itemManagementUiState.apply {
                        value = (value as ItemManagementUiState.ToBeDeleted).copy(
                            state = ItemUiState.Error(throwable)
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.NoContent -> {
                            val elapsed: Long = System.currentTimeMillis() - start

                            delay(1000 - elapsed)

                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.ToBeDeleted).copy(
                                    state = ItemUiState.Success
                                )
                            }
                        }

                        ResponseState.Forbidden,
                        ResponseState.NotFound,
                        ResponseState.Unauthorized -> {
                            _itemManagementUiState.apply {
                                value = (value as ItemManagementUiState.ToBeDeleted).copy(
                                    state = ItemUiState.Error(response.getExceptionOrNull()!!)
                                )
                            }
                        }

                        else -> throw IllegalStateException("Unexpected response state: $response")
                    }
                }
        }
    }
}
