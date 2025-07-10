package com.cocot3ro.gh.almacen.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.state.ResponseState
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
import com.cocot3ro.gh.almacen.domain.usecase.SetUpConnectionValuesUseCase
import com.cocot3ro.gh.almacen.domain.usecase.TestConnectionUseCase
import com.cocot3ro.gh.almacen.domain.state.UiState
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

        _uiState.value = UiState.Loading to UiState.Idle

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

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
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(1000 - elapsed)

                    _uiState.value = UiState.Success(
                        // Not null beacause it was already checked in the SplashScreen
                        value = preferences.host!! to preferences.port!!
                    ) to UiState.Idle

                    testConnection(
                        host = preferences.host,
                        port = preferences.port,
                        isReloading = false
                    )
                }
        }
    }

    fun retry() {
        if (_uiState.value.first !is UiState.Success<*> || _uiState.value.second !is UiState.Error<*>)
            return

        val connectionValues = (_uiState.value.first as UiState.Success<*>).value as Pair<*, *>
        val host: String = connectionValues.first as String
        val port: UShort = connectionValues.second as UShort

        testConnection(host = host, port = port, isReloading = true)
    }

    private fun testConnection(host: String, port: UShort, isReloading: Boolean) {
        _uiState.value =
            _uiState.value.copy(
                second = if (!isReloading) UiState.Loading else UiState.Reloading(
                    Unit
                )
            )

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            testConnectionUseCase(host, port)
                .catch { throwable: Throwable ->
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(500 - elapsed)

                    _uiState.value = _uiState.value.copy(
                        second = UiState.Error(
                            cause = throwable,
                            retry = false,
                            cache = null
                        )
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { result: ResponseState ->
                    when (result) {
                        is ResponseState.OK<*> -> {

                            val elapsed: Long = System.currentTimeMillis() - start
                            delay(1000 - elapsed)

                            _uiState.value = _uiState.value.copy(
                                second = UiState.Success(value = result)
                            )

                            completeConnection(host, port)
                        }

                        is ResponseState.Error -> {
                            val elapsed: Long = System.currentTimeMillis() - start
                            delay(500 - elapsed)

                            _uiState.value = _uiState.value.copy(
                                second = UiState.Error(
                                    cause = result.cause,
                                    retry = false,
                                    cache = null
                                )
                            )
                        }

                        else -> Unit
                    }
                }
        }
    }

    private fun completeConnection(host: String, port: UShort) {
        setUpConnectionValuesUseCase(host = host, port = port)
    }
}
