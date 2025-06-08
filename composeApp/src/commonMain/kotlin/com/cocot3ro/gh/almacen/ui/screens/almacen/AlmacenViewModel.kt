package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrDefault
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrNull
import com.cocot3ro.gh.almacen.domain.usecase.ManageAlmacenItemUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.ui.state.UiState
import com.cocot3ro.gh.almacen.ui.state.ext.isLoadingOrReloading
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

    val loggedUser: AlmacenUserDomain by lazy {
        manageLoginUsecase.getLoggedUser() ?: throw IllegalStateException("User not logged in")
    }

    var filter: String by mutableStateOf("")
        private set

    var filterMode: FilterMode by mutableStateOf(FilterMode.NAME)
        private set

    var sortMode: SortMode by mutableStateOf(SortMode.ID)
        private set

    var displayMode: DisplayMode by mutableStateOf(DisplayMode.LIST)
        private set

    var showLowStockFirst: Boolean by mutableStateOf(true)
        private set

    private val _itemManagementUiState: MutableStateFlow<ItemManagementUiState> =
        MutableStateFlow(ItemManagementUiState.Idle)
    val itemManagementUiState: StateFlow<ItemManagementUiState> =
        _itemManagementUiState.asStateFlow()

    private var fetchJob: Job? = null

    private var _items: List<AlmacenItemDomain> = emptyList()
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

        _uiState.value = UiState.Reloading
        fetch()
    }

    private fun onError(cause: Throwable, retry: Boolean) {
        _itemManagementUiState.value = ItemManagementUiState.Idle

        _uiState.value = UiState.Error(
            cause = cause,
            retry = retry,
            cache = _items.filter().sort()
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
                            _items = response.data as List<AlmacenItemDomain>

                            when (val state: ItemManagementUiState = _itemManagementUiState.value) {
                                is ItemManagementUiState.Idle -> Unit

                                is ItemManagementUiState.ToBeDeleted -> {
                                    val itemId: Long = state.item.id
                                    _items.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }

                                is ItemManagementUiState.UnexpectedDeleted -> Unit

                                is ItemManagementUiState.AddStock -> {
                                    val itemId: Long = state.item.id
                                    _items.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }

                                is ItemManagementUiState.Edit -> {
                                    val itemId: Long = state.item.id
                                    _items.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }

                                is ItemManagementUiState.TakeStock -> {
                                    val itemId: Long = state.item.id
                                    _items.firstOrNull { it.id == itemId }?.let { item ->
                                        _itemManagementUiState.value = state.copy(item = item)
                                    } ?: run {
                                        _itemManagementUiState.value =
                                            ItemManagementUiState.UnexpectedDeleted(state.item)
                                    }
                                }
                            }

                            _uiState.value = UiState.Success(_items.filter().sort())
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
        if (filter.isEmpty() || filter.isBlank()) return this

        return filter { item: AlmacenItemDomain ->
            item.name.contains(filter, ignoreCase = true)
        }
    }

    private fun List<AlmacenItemDomain>.sort(): List<AlmacenItemDomain> = partition {
        showLowStockFirst && it.quantity <= it.minimum
    }.let { (lowStock: List<AlmacenItemDomain>, rest: List<AlmacenItemDomain>) ->
        val sortedRest: List<AlmacenItemDomain> = when (sortMode) {
            SortMode.ID -> rest.sortedBy(AlmacenItemDomain::id)
            SortMode.NAME -> rest.sortedBy(AlmacenItemDomain::name)
            SortMode.QUANTITY -> rest.sortedBy(AlmacenItemDomain::quantity)
        }
        lowStock + sortedRest
    }

    fun updateFilter(filter: String) {
        if (_uiState.value !is UiState.Success<*>) return
        this.filter = filter
        _uiState.value = UiState.Success(_items.filter().sort())
    }

    fun updateFilterMode(filterMode: FilterMode) {
        if (_uiState.value !is UiState.Success<*>) return
        this.filterMode = filterMode
        _uiState.value = UiState.Success(_items.filter().sort())
    }

    fun updateSortBy(sortBy: SortMode) {
        if (_uiState.value !is UiState.Success<*>) return
        this.sortMode = sortBy
        _uiState.value = UiState.Success(_items.filter().sort())
    }

    fun updateShowLowStockFirst(showLowStockFirst: Boolean) {
        if (_uiState.value !is UiState.Success<*>) return
        this.showLowStockFirst = showLowStockFirst
        _uiState.value = UiState.Success(_items.filter().sort())
    }

    fun toggleDisplayMode() {
        this.displayMode = DisplayMode.entries.let { it[(displayMode.ordinal + 1) % it.size] }
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

    fun deleteItem(item: AlmacenItemDomain) {
        _itemManagementUiState.value = ItemManagementUiState.ToBeDeleted(item, ItemUiState.Waiting)
    }

    fun clearItemManagementUiState() {
        _itemManagementUiState.value = ItemManagementUiState.Idle
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

            manageAlmacenItemUseCase.takeStock(item, amount)
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

    fun onEdit() {
        _itemManagementUiState.apply {
            value = (value as ItemManagementUiState.Edit).copy(state = ItemUiState.Loading)
        }
    }

    fun onDelete() {
        _itemManagementUiState.apply {
            value = (value as ItemManagementUiState.ToBeDeleted).copy(state = ItemUiState.Loading)
        }
    }
}
