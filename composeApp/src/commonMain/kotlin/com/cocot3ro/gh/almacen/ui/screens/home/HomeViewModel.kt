package com.cocot3ro.gh.almacen.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.state.TestConnectionResult
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
import com.cocot3ro.gh.almacen.domain.usecase.SetUpConnectionValuesUseCase
import com.cocot3ro.gh.almacen.domain.usecase.TestConnectionUseCase
import com.cocot3ro.gh.almacen.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import kotlin.time.Duration.Companion.seconds

@KoinViewModel
class HomeViewModel(
    @Provided private val managePreferencesUseCase: ManagePreferencesUseCase,
    @Provided private val testConnectionUseCase: TestConnectionUseCase,
    @Provided private val setUpConnectionValuesUseCase: SetUpConnectionValuesUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<Pair<UiState, UiState>> =
        MutableStateFlow(UiState.Idle to UiState.Idle)

    val uiState: StateFlow<Pair<UiState, UiState>> = _uiState
        .onStart { getPreferences() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Idle to UiState.Idle
        )

    private fun getPreferences() {

        _uiState.value = UiState.Loading(firstLoad = true) to UiState.Idle

        viewModelScope.launch {
            delay(1.seconds)

            managePreferencesUseCase.getPreferences()
                .catch { throwable: Throwable ->
                    // TODO: Report to Kotzilla
                    _uiState.value = UiState.Error(
                        cause = throwable,
                        retry = false,
                        cache = null
                    ) to UiState.Idle
                }
                .flowOn(Dispatchers.IO)
                .collect { preferences ->
                    _uiState.value = UiState.Success(
                        // Not null beacause it was already checked in the SplashScreen
                        value = preferences.host!! to preferences.port!!
                    ) to UiState.Idle

                    testConnection(preferences.host, preferences.port)
                }
        }
    }

    fun retry() {
        if (_uiState.value.first !is UiState.Success<*> || _uiState.value.second !is UiState.Error<*>)
            return

        val connectionValues = (_uiState.value.first as UiState.Success<*>).value as Pair<*, *>
        val host: String = connectionValues.first as String
        val port: UShort = connectionValues.second as UShort

        testConnection(host, port)
    }

    // TODO: Download the SSL certificate from the server once the connection is established
    private fun testConnection(host: String, port: UShort) {
        _uiState.value = _uiState.value.copy(second = UiState.Loading(firstLoad = true))

        viewModelScope.launch {
            delay(1.seconds)

            testConnectionUseCase(host, port)
                .catch { throwable: Throwable ->
                    _uiState.value = _uiState.value.copy(
                        second = UiState.Error(
                            cause = throwable,
                            retry = false,
                            cache = null
                        )
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { result: TestConnectionResult ->
                    when (result) {
                        TestConnectionResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                second = UiState.Success(value = result)
                            )

                            completeConnection(host, port)
                        }

                        TestConnectionResult.ServiceUnavailable -> {
                            _uiState.value = _uiState.value.copy(
                                second = UiState.Success(value = result)
                            )
                        }

                        is TestConnectionResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                second = UiState.Error(
                                    cause = result.cause,
                                    retry = false,
                                    cache = null
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun completeConnection(host: String, port: UShort) {
        setUpConnectionValuesUseCase(host = host, port = port)
    }
}
