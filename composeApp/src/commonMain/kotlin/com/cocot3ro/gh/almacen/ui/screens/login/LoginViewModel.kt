package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.domain.state.LoginResult
import com.cocot3ro.gh.almacen.domain.usecase.GetAlmacenUsersUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.ui.state.UiState
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class LoginViewModel(
    @Provided private val manageLoginUsecase: ManageLoginUsecase,
    @Provided private val getAlmacenUsersUseCase: GetAlmacenUsersUseCase
) : ViewModel() {

    private var fetchJob: Job? = null

    private val _usersState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val usersState: StateFlow<UiState> = _usersState
        .onStart {
            firstFetch()
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

    fun setUserToLogin(user: AlmacenUserDomain?) {
        if (user == null) {
            _loginUiState.value = LoginUiState.Idle
            password = ""
        } else if (user.requiresPassword) {
            _loginUiState.value = LoginUiState.Waiting(user = user)
        } else {
            login(user = user, password = null)
        }
    }

    private fun firstFetch() {
        _usersState.value = UiState.Loading(firstLoad = true)
        fetchUsers()
    }

    fun refreshUsers() {
        if (_usersState.value is UiState.Loading) return

        _usersState.value = UiState.Loading(firstLoad = false)
        fetchUsers()
    }

    private fun fetchUsers() {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            getAlmacenUsersUseCase()
                .catch { cause: Throwable ->
                    _usersState.value = UiState.Error(
                        cause = cause,
                        retry = false,
                        cache = (_usersState.value as? UiState.Success<*>)?.value as? List<*>
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { users ->
                    _usersState.value = UiState.Success(users)
                }
        }
    }

    fun login(user: AlmacenUserDomain, password: String?) {
        if (_loginUiState.value is LoginUiState.Loading) return

        _loginUiState.value = LoginUiState.Loading(user)

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            manageLoginUsecase.logIn(user, password)
                .catch {
                    _loginUiState.value = LoginUiState.Error(user = user, cause = it)
                }
                .flowOn(Dispatchers.IO)
                .collect { result ->

                    // Wait for at least 500 milliseconds
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(500 - elapsed)

                    _loginUiState.value = when (result) {
                        is LoginResult.Success -> LoginUiState.Success(user = user)

                        LoginResult.Unauthorized -> LoginUiState.Fail(user = user)

                        is LoginResult.Error -> LoginUiState.Error(
                            user = user,
                            cause = result.cause
                        )
                    }
                }
        }
    }
}
