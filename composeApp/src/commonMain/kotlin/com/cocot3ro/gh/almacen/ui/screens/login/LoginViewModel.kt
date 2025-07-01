package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.state.ext.getExceptionOrDefault
import com.cocot3ro.gh.almacen.domain.usecase.GetUsersUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.ui.state.UiState
import com.cocot3ro.gh.almacen.ui.state.ext.isLoadingOrReloading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class LoginViewModel(
    @Provided private val manageLoginUsecase: ManageLoginUsecase,
    @Provided private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private var fetchJob: Job? = null

    private var _usersCache: List<UserDomain> = emptyList()
    private val _usersState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val usersState: StateFlow<UiState> = _usersState
        .onStart {
            _usersState.value = UiState.Loading
            fetchUsers()
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Idle
        )

    private val _loginUiState: MutableStateFlow<LoginUiState> = MutableStateFlow(LoginUiState.Idle)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    var password: String by mutableStateOf("")
        private set

    fun updatePassword(newPassword: String) {
        password = newPassword
    }

    fun setUserToLogin(user: UserDomain?) {
        when {
            user == null -> {
                _loginUiState.value = LoginUiState.Idle
                password = ""
            }

            user.requiresPassword -> {
                _loginUiState.value = LoginUiState.Waiting(user = user)
            }

            else -> {
                login(user = user, password = null)
            }
        }
    }

    fun refreshUsers() {
        if (_usersState.value.isLoadingOrReloading()) return

        _usersState.value = UiState.Reloading(_usersCache)
        fetchUsers()
    }

    private fun onUsersError(cause: Throwable, retry: Boolean) {
        _usersState.value = UiState.Error(
            cause = cause,
            retry = retry,
            cache = _usersCache
        )
    }

    private fun fetchUsers() {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
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

    fun login(user: UserDomain, password: String?) {
        if (_loginUiState.value is LoginUiState.Loading) return

        _loginUiState.value = LoginUiState.Loading(user)

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageLoginUsecase.logIn(user, password)
                .catch {
                    _loginUiState.value = LoginUiState.Error(user = user, cause = it)
                }
                .flowOn(Dispatchers.IO)
                .collect { result: ResponseState ->

                    // Wait for at least 1 second
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(1000 - elapsed)

                    when (result) {
                        is ResponseState.OK<*> -> {
                            _loginUiState.value = LoginUiState.Success(user = user)
                        }

                        is ResponseState.BadRequest,
                        is ResponseState.Unauthorized -> {
                            _loginUiState.value = LoginUiState.Fail(user = user)
                        }

                        is ResponseState.Error -> {
                            _loginUiState.value = LoginUiState.Error(
                                user = user,
                                cause = result.cause
                            )
                        }

                        else -> Unit
                    }
                }
        }
    }
}
