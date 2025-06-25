package com.cocot3ro.gh.almacen.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.BuildConfig
import com.cocot3ro.gh.almacen.domain.model.AppVersionDomain
import com.cocot3ro.gh.almacen.domain.model.PreferenceItem
import com.cocot3ro.gh.almacen.domain.model.getPlatform
import com.cocot3ro.gh.almacen.domain.usecase.GetLatestVersionUseCase
import com.cocot3ro.gh.almacen.domain.usecase.ManageLoginUsecase
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SplashViewModel(
    @Provided private val managePreferencesUseCase: ManagePreferencesUseCase,
    @Provided private val manageLoginUsecase: ManageLoginUsecase,
    @Provided private val getLatestVersionUseCase: GetLatestVersionUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Idle)
    val uiState: StateFlow<SplashUiState> = _uiState
        .onStart { load() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = SplashUiState.Idle
        )

    private fun load() {
        _uiState.value = SplashUiState.Loading

        val start: Long = System.currentTimeMillis()
        viewModelScope.launch {
            val logoutDeferred: Deferred<Unit> = async(Dispatchers.IO) {
                manageLoginUsecase.logOut()
            }

            val updatesDeferred: Deferred<Result<AppVersionDomain>> = async {
                getLatestVersionUseCase.invoke(getPlatform().appDistribution)
            }

            val prefsDeferred: Deferred<PreferenceItem> = async {
                managePreferencesUseCase.getPreferences()
                    .flowOn(Dispatchers.IO)
                    .first()
            }

            updatesDeferred.await().onSuccess { appVersion: AppVersionDomain ->
                val newVersionCode: Int = appVersion.version.versionName.split(".")
                    .map(String::toInt)
                    .reduce { acc, i -> acc * 100 + i }

                if (BuildConfig.VERSION_CODE < newVersionCode) {
                    logoutDeferred.await()

                    delay(1000 - System.currentTimeMillis() - start)
                    _uiState.value = SplashUiState.UpdateRequired(appVersion.version)

                    return@launch
                }
            }

            prefsDeferred.await().let { prefs: PreferenceItem ->
                if (prefs.host == null || prefs.port == null) {
                    logoutDeferred.await()

                    delay(1000 - System.currentTimeMillis() - start)
                    _uiState.value = SplashUiState.SetupRequired
                } else {
                    logoutDeferred.await()

                    delay(1000 - System.currentTimeMillis() - start)
                    _uiState.value = SplashUiState.SplashUiFinished
                }
            }
        }
    }
}