package com.cocot3ro.gh.almacen.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
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

@KoinViewModel
class SplashViewModel(
    @Provided private val managePreferencesUseCase: ManagePreferencesUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
        .onStart { fetch() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UiState.Idle
        )

    private fun fetch() {
        _uiState.value = UiState.Loading(firstLoad = true)

        viewModelScope.launch {
            val start: Long = System.currentTimeMillis()

            managePreferencesUseCase.getPreferences()
                .catch { throwable: Throwable ->
                    if (_uiState.value is UiState.Success<*> || _uiState.value is UiState.Error<*>)
                        return@catch

                    // TODO: Report to Kotzilla

                    // Wait for at least 1 second
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(1000 - elapsed)

                    _uiState.value = UiState.Error(
                        cause = throwable,
                        retry = false,
                        cache = null
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { prefs ->
                    if (_uiState.value is UiState.Success<*> || _uiState.value is UiState.Error<*>)
                        return@collect

                    // Wait for at least 1 second
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(1000 - elapsed)

                    _uiState.value = UiState.Success(prefs.host == null || prefs.port == null)
                }
        }
    }
}