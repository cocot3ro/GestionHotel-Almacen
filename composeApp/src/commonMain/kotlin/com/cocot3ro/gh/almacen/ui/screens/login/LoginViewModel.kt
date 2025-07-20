package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.AlmacenStoreDomain
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.LoginUiState
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.UiState
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrDefault
import com.cocot3ro.gh.almacen.domain.state.ext.getStore
import com.cocot3ro.gh.almacen.domain.state.ext.getUser
import com.cocot3ro.gh.almacen.domain.state.ext.isLoadingOrReloading
import com.cocot3ro.gh.almacen.domain.usecase.GetAlmacenStoresUseCase
import com.cocot3ro.gh.almacen.domain.usecase.GetUsersUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class LoginViewModel(
    @Provided private val manageLoginUsecase: ManageLoginUsecase,
    @Provided private val getUsersUseCase: GetUsersUseCase,
    @Provided private val getAlmacenStoresUseCase: GetAlmacenStoresUseCase
) : ViewModel() {

    private var fetchUsersJob: Job? = null
    private var _usersCache: List<UserDomain> = emptyList()
    private val _usersState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val usersState: StateFlow<UiState> = _usersState
        .onStart {
            _usersState.value = UiState.Loading
            fetchUsers()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = UiState.Idle
        )

    private var fetchStoresJob: Job? = null
    private val _storesCache: List<UserDomain> = emptyList()
    private val _storesState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val storesState: StateFlow<UiState> = _storesState
        .onStart {
            _storesState.value = UiState.Loading
            fetchStores()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = UiState.Idle
        )

    private val _loginUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = LoginUiState.Idle
    )

    var password: String by mutableStateOf("")
        private set

    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun setUserToLogin(user: UserDomain?) {
        password = ""
        setLoginData(user, _loginUiState.value.getStore())
    }

    fun setStoreToLogin(store: AlmacenStoreDomain?) {
        setLoginData(_loginUiState.value.getUser(), store)
    }

    private fun setLoginData(user: UserDomain?, store: AlmacenStoreDomain?) {
        _loginUiState.value = LoginUiState.Waiting(user = user, store = store)

        if (user == null || store == null || user.requiresPassword) return
    }

    fun refresh() {
        if (_usersState.value.isLoadingOrReloading().not()) {
            _usersState.value = UiState.Reloading(_usersCache)
            fetchUsers()
        }

        if (_storesState.value.isLoadingOrReloading().not()) {
            _storesState.value = UiState.Reloading(_storesCache)
            fetchStores()
        }
    }

    private fun onUsersError(cause: Throwable, retry: Boolean) {
        _usersState.value = UiState.Error(
            cause = cause,
            retry = retry,
            cache = _usersCache
        )
    }

    private fun fetchUsers() {
        fetchUsersJob?.cancel()

        fetchUsersJob = viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            getUsersUseCase()
                .retry(retries = 3) { throwable: Throwable ->
                    onUsersError(cause = throwable, retry = true)

                    true
                }
                .catch { cause: Throwable ->
                    onUsersError(cause = cause, retry = false)
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.OK<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            _usersCache = response.data as List<UserDomain>

                            val elapsed: Long = System.currentTimeMillis() - start
                            delay(500 - elapsed)

                            _usersState.value = UiState.Success(_usersCache)
                        }

                        else -> {
                            onUsersError(cause = response.getExceptionOrDefault(), retry = false)
                        }
                    }
                }
        }
    }

    private fun fetchStores() {
        fetchStoresJob?.cancel()

        fetchStoresJob = viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            getAlmacenStoresUseCase()
                .retry(retries = 3) { throwable: Throwable ->
                    _storesState.value = UiState.Error(
                        cause = throwable,
                        retry = true,
                        cache = _storesCache
                    )

                    true
                }
                .catch { cause: Throwable ->
                    _storesState.value = UiState.Error(
                        cause = cause,
                        retry = false,
                        cache = _storesCache
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { response: ResponseState ->
                    when (response) {
                        is ResponseState.OK<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val stores: List<UserDomain> = response.data as List<UserDomain>

                            val elapsed: Long = System.currentTimeMillis() - start
                            delay(500 - elapsed)

                            _storesState.value = UiState.Success(stores)
                        }

                        else -> {
                            _storesState.value = UiState.Error(
                                cause = response.getExceptionOrDefault(),
                                retry = false,
                                cache = _storesCache
                            )
                        }
                    }
                }
        }
    }

    fun login() {
        if (_loginUiState.value is LoginUiState.Idle) return
        if (_loginUiState.value is LoginUiState.Loading) return

        val user: UserDomain = _loginUiState.value.getUser() ?: return
        val store: AlmacenStoreDomain = _loginUiState.value.getStore() ?: return

        _loginUiState.value = LoginUiState.Loading(user, store)

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageLoginUsecase.logIn(user, password, store)
                .catch {
                    _loginUiState.value = LoginUiState.Error(user = user, store = store, cause = it)
                }
                .flowOn(Dispatchers.IO)
                .collect { result: ResponseState ->

                    // Wait for at least 1 second
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(1000 - elapsed)

                    when (result) {
                        is ResponseState.OK<*> -> {
                            _loginUiState.value = LoginUiState.Success(user = user, store = store)
                        }

                        is ResponseState.BadRequest,
                        is ResponseState.Unauthorized -> {
                            _loginUiState.value = LoginUiState.Fail(user = user, store = store)
                        }

                        is ResponseState.Error -> {
                            _loginUiState.value = LoginUiState.Error(
                                user = user,
                                store = store,
                                cause = result.cause
                            )
                        }

                        else -> Unit
                    }
                }
        }
    }
}
