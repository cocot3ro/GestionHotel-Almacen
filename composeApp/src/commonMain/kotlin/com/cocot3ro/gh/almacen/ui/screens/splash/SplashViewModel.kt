package com.cocot3ro.gh.almacen.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.usecase.GetPreferencesUseCase
import com.cocot3ro.gh.almacen.ui.screens.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SplashViewModel(
    @Provided private val getPreferencesUseCase: GetPreferencesUseCase,
) : ViewModel() {

    private val _firstTimeUiState: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val firstTimeUiState: StateFlow<UiState>
        get() {
            viewModelScope.launch {
                getPreferencesUseCase()
                    .catch { throwable ->
                        _firstTimeUiState.value = UiState.Error(throwable)
                    }
                    .flowOn(Dispatchers.IO)
                    .collect { prefs ->
                        _firstTimeUiState.value = UiState.Success(prefs.firstTime)
                    }
            }
            return _firstTimeUiState.asStateFlow()
        }
}