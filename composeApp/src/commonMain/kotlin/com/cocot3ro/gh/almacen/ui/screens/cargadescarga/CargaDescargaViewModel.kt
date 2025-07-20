package com.cocot3ro.gh.almacen.ui.screens.cargadescarga

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.util.fastCoerceIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.CargaDescargaData
import com.cocot3ro.gh.almacen.domain.model.CargaDescargaMode
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.UiState
import com.cocot3ro.gh.almacen.domain.state.ex.UnauthorizedException
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrDefault
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrNull
import com.cocot3ro.gh.almacen.domain.usecase.ManageAlmacenItemUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided
import java.net.SocketException
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class CargaDescargaViewModel(
    @InjectedParam val cargaDescargaMode: CargaDescargaMode,
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
    var searchText: String by mutableStateOf("")

    var newItem: CargaDescargaData? by mutableStateOf(null)
        private set

    private val _cargaUiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val cargaUiState: StateFlow<UiState> = _cargaUiState.asStateFlow()
    private val _cargaMap: SnapshotStateMap<AlmacenItemDomain, Int?> = mutableStateMapOf()
    val cargaMap: Map<AlmacenItemDomain, Int?> get() = _cargaMap.toMap()

    private var itemsJob: Job? = null
    private var _itemsCache: List<AlmacenItemDomain> = emptyList()
    private val _itemsUiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val itemsUiState: StateFlow<UiState> = _itemsUiState
        .onStart {
            _itemsUiState.value = UiState.Loading
            fetchItems()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = UiState.Idle
        )

    fun performStockUpdate() {
        if (_cargaUiState.value is UiState.Loading) return
        if (_cargaUiState.value is UiState.Success<*>) return

        viewModelScope.launch {

            _cargaUiState.value = UiState.Loading

            val items = _cargaMap.mapNotNullTo(
                destination = mutableListOf(),
                transform = { (k: AlmacenItemDomain, v: Int?) -> v?.let { k to v } }
            ).toMap()

            when (cargaDescargaMode) {
                CargaDescargaMode.CARGA -> manageAlmacenItemUseCase.addMultipleStock(items)

                CargaDescargaMode.DESCARGA -> manageAlmacenItemUseCase.takeMultipleStock(
                    items = items,
                    store = loggedStore
                )
            }
                .catch {
                    _cargaUiState.value = UiState.Error(
                        cause = it,
                        retry = false,
                        cache = null
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {

                        is ResponseState.OK<*>,
                        ResponseState.PartialContent,
                        ResponseState.NoContent -> {
                            _cargaUiState.value = UiState.Success(value = Unit)
                            _cargaMap.clear()
                        }

                        ResponseState.Unauthorized -> {
                            _cargaUiState.value = UiState.Error(
                                cause = response.getExceptionOrNull()!!,
                                retry = false,
                                cache = null
                            )
                        }

                        else -> {
                            throw IllegalStateException("Unexpected response state: $response")
                        }
                    }
                }
        }
    }

    private fun onError(cause: Throwable, retry: Boolean) {

        _itemsUiState.value = UiState.Error(
            cause = cause,
            retry = retry,
            cache = _itemsCache.filter()
        )
    }

    private fun fetchItems() {
        itemsJob?.cancel()

        itemsJob = viewModelScope.launch {
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

                            _cargaMap.keys.forEach { item ->
                                if (item !in _itemsCache) {
                                    _cargaMap[item] = null
                                }
                            }

                            _itemsUiState.value = UiState.Success(_itemsCache.filter())
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
        if (searchText.isBlank()) return this

        return filter { item: AlmacenItemDomain ->
            item.name.contains(searchText, ignoreCase = true)
        }
    }

    fun updateShowSearch(showSearch: Boolean) {
        this.showSearch = showSearch
    }

    fun updateFilter(filter: String) {
        this.searchText = filter
        _itemsUiState.value = UiState.Success(_itemsCache.filter())
    }

    fun clearSelectItem() {
        newItem = null
    }

    fun selectItem(item: AlmacenItemDomain) {
        newItem = CargaDescargaData(
            item = item,
            amount = _cargaMap[item] ?: 0,
            min = 0,
            max = when (cargaDescargaMode) {
                CargaDescargaMode.CARGA -> Int.MAX_VALUE - item.quantity
                CargaDescargaMode.DESCARGA -> item.quantity
            }
        )
    }

    fun selectByBarcode(input: String): Boolean {
        val barcode: Long = input.toLongOrNull() ?: return false

        val item: AlmacenItemDomain = _itemsCache.find { it.barcodes.contains(barcode) }
            ?: return false

        selectItem(item)

        return true
    }

    fun addItem() {
        val (item: AlmacenItemDomain, amount: Int?) = newItem ?: return

        if (amount == null) return

        if (amount <= 0) return

        _cargaMap[item] = amount

        newItem = null
    }

    fun removeItem(item: AlmacenItemDomain) {
        _cargaMap.remove(item)
    }

    fun updateAmount(input: String) {
        if (newItem == null) return

        val amount: Int? =
            input.replace("""\D""".toRegex(), "").toLongOrNull()
                ?.fastCoerceIn(0, newItem!!.max.toLong())
                ?.toInt()


        newItem = newItem?.copy(
            amount = amount?.fastCoerceIn(
                minimumValue = newItem!!.min,
                maximumValue = newItem!!.max
            )
        )
    }

    fun decrementAmount() {
        if (newItem == null) return

        newItem = when {
            newItem!!.amount == null -> {
                newItem?.copy(amount = 0)
            }

            newItem!!.amount!! <= newItem!!.min -> {
                newItem?.copy(amount = newItem!!.min)
            }

            else -> {
                newItem?.copy(amount = newItem!!.amount!! - 1)
            }
        }
    }

    fun incrementAmount() {
        if (newItem == null) return

        newItem = when {
            newItem!!.amount == null -> {
                newItem?.copy(amount = 1)
            }

            newItem!!.amount!! >= newItem!!.max -> {
                newItem?.copy(amount = newItem!!.max)
            }

            else -> {
                newItem?.copy(amount = newItem!!.amount!! + 1)
            }
        }
    }

    fun dismiss() {
        showSearch = false
        searchText = ""
        newItem = null
        _cargaUiState.value = UiState.Idle
        _cargaMap.clear()
        itemsJob?.cancel()
        _itemsUiState.value = UiState.Idle
        _itemsCache = emptyList()
    }
}
