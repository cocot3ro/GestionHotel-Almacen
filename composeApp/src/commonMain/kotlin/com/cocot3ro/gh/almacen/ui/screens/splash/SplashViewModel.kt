package com.cocot3ro.gh.almacen.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
import com.cocot3ro.gh.almacen.ui.state.UiState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SplashViewModel(
    @Provided private val managePreferencesUseCase: ManagePreferencesUseCase,
    @Provided private val manageLoginUsecase: ManageLoginUsecase
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
        .onStart { load() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = UiState.Idle
        )

    private fun load() {
        _uiState.value = UiState.Loading

        val start: Long = System.currentTimeMillis()
        viewModelScope.launch {
            val deferred: Deferred<Unit> = async(Dispatchers.IO) {
                manageLoginUsecase.logOut()
            }

            managePreferencesUseCase.getPreferences()
                .flowOn(Dispatchers.IO)
                .collect { prefs ->
                    if (_uiState.value is UiState.Success<*> || _uiState.value is UiState.Error<*>)
                        return@collect

                    deferred.await()

                    // Wait for at least 1 second
                    val elapsed: Long = System.currentTimeMillis() - start
                    delay(1000 - elapsed)

                    _uiState.value = UiState.Success(prefs.host == null || prefs.port == null)
                }
        }
    }
}