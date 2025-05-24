package com.cocot3ro.gh.almacen.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.state.TestConnectionResult
import com.cocot3ro.gh.almacen.domain.usecase.ManagePreferencesUseCase
import com.cocot3ro.gh.almacen.domain.usecase.TestConnectionUseCase
import com.cocot3ro.gh.almacen.ui.state.UiState
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
class SettingsViewModel(
    @Provided private val testConnectionUseCase: TestConnectionUseCase,
    @Provided private val managePreferencesUseCase: ManagePreferencesUseCase,
) : ViewModel() {

    private val _uiSate: MutableStateFlow<UiState> = MutableStateFlow(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiSate.asStateFlow()

    var host: TextFieldValue<String> by mutableStateOf(
        TextFieldValue(value = "", status = TextFieldStatus.IDLE)
    )
        private set

    var port: TextFieldValue<String> by mutableStateOf(
        TextFieldValue(value = "", status = TextFieldStatus.IDLE)
    )
        private set

    private val hostFormatRegex: Regex = """\d{1,3}(\.\d{1,3}){3}""".toRegex()

    private val hostValueRegex: Regex =
        """(\b25[0-5]|\b2[0-4][0-9]|\b[01]?[0-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}""".toRegex()

    fun updateHost(input: String) {
        val host = input.trimStart()

        if (host.length > 15) return

        when {
            host.isBlank() -> {
                this.host = TextFieldValue(
                    value = host,
                    status = TextFieldStatus.EMPTY_VALUE
                )
            }

            !host.matches(hostFormatRegex) -> {
                this.host = TextFieldValue(
                    value = host,
                    status = TextFieldStatus.INVALID_FORMAT
                )
            }

            !host.matches(hostValueRegex) -> {
                this.host = TextFieldValue(
                    value = host,
                    status = TextFieldStatus.INVALID_VALUE
                )
            }

            else -> {
                this.host = TextFieldValue(
                    value = host,
                    status = TextFieldStatus.VALID
                )
            }
        }

        _uiSate.value = UiState.Idle
    }

    fun updatePort(input: String) {
        val port = input.replace("""\D""".toRegex(), "")

        if (port.length > 5) return

        when (port.toIntOrNull()) {
            null -> {
                this.port = TextFieldValue(
                    value = port,
                    status = TextFieldStatus.INVALID_FORMAT
                )
            }

            !in (0x0..0xFFFF) -> {
                this.port = TextFieldValue(
                    value = port,
                    status = TextFieldStatus.INVALID_VALUE
                )
            }

            else -> {
                this.port = TextFieldValue(
                    value = port,
                    status = TextFieldStatus.VALID
                )
            }
        }

        _uiSate.value = UiState.Idle
    }

    fun testConnection() {

        updateHost(this.host.value)
        updatePort(this.port.value)

        if (this.host.status != TextFieldStatus.VALID ||
            this.port.status != TextFieldStatus.VALID
        ) return

        _uiSate.value = UiState.Loading(firstLoad = true)

        viewModelScope.launch {
            testConnectionUseCase(host = host.value, port = port.value.toUShort())
                .catch { throwable ->
                    _uiSate.value = UiState.Error(
                        cause = throwable,
                        retry = false,
                        cache = null
                    )
                }
                .flowOn(Dispatchers.IO)
                .collect { result: TestConnectionResult ->
                    when (result) {
                        TestConnectionResult.Success,
                        TestConnectionResult.ServiceUnavailable -> {
                            _uiSate.value = UiState.Success(value = result)
                        }

                        is TestConnectionResult.Error -> {
                            _uiSate.value = UiState.Error(
                                cause = result.cause,
                                retry = false,
                                cache = null
                            )
                        }
                    }
                }
        }
    }

    suspend fun completeSetup() {
        if (this.host.status != TextFieldStatus.VALID ||
            this.port.status != TextFieldStatus.VALID ||
            this._uiSate.value !is UiState.Success<*>
        ) return

        managePreferencesUseCase.setHost(host = host.value)
        managePreferencesUseCase.setPort(port = port.value.toUShort())
    }
}
