package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.AlmacenItemDomain
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.domain.usecase.ManageAlmacenItemUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import java.net.SocketException

@KoinViewModel
class AlmacenViewModel(
    @Provided private val manageLoginUsecase: ManageLoginUsecase,
    @Provided private val manageAlmacenItemUseCase: ManageAlmacenItemUseCase
) : ViewModel() {

    val loggedUser: AlmacenUserDomain
        get() = manageLoginUsecase.getLoggedUser()
            ?: throw IllegalStateException("User not logged in")

    private var fetchJob: Job? = null

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState: StateFlow<UiState>
        get() = _uiState.asStateFlow().also {
            when {
                _uiState.value is UiState.Idle -> {
                    _uiState.value = UiState.Loading(firstLoad = true)
                    fetch()
                }

                (_uiState.value as? UiState.Error<*>)?.retry == true -> {
                    _uiState.value = UiState.Loading(firstLoad = false)
                    fetch()
                }

                else -> Unit
            }
        }

    fun refresh() {
        if (_uiState.value is UiState.Loading) return

        _uiState.value = UiState.Loading(firstLoad = false)
        fetch()
    }

    private fun fetch() {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            manageAlmacenItemUseCase.getAll()
                .catch { throwable: Throwable ->
                    val cache: List<*> = when (val v = _uiState.value) {
                        is UiState.Success<*> -> v.value as List<*>
                        is UiState.Error<*> -> v.cache as List<*>
                        else -> emptyList<AlmacenItemDomain>()
                    }

                    when (throwable) {
                        is SocketException -> {
                            _uiState.value =
                                UiState.Error(cause = throwable, retry = true, cache = cache)
                        }

                        else -> {
                            _uiState.value =
                                UiState.Error(cause = throwable, retry = false, cache = cache)
                        }
                    }

                }
                .flowOn(Dispatchers.IO)
                .collect { items: List<AlmacenItemDomain> ->
                    _uiState.value = UiState.Success(items)
                }
        }
    }

    fun delete(item: AlmacenItemDomain) {
        viewModelScope.launch(Dispatchers.IO) {
            manageAlmacenItemUseCase.delete(item)
        }
    }
}
