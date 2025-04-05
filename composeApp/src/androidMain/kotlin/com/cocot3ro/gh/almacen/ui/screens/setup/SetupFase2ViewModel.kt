package com.cocot3ro.gh.almacen.ui.screens.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocot3ro.gh.almacen.domain.usecase.SavePreferencesUseCase
import com.cocot3ro.gh.almacen.domain.usecase.TestConnectionUseCase
import com.cocot3ro.gh.almacen.ui.screens.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SetupFase2ViewModel(
    private val testConnectionUseCase: TestConnectionUseCase,
    private val savePreferencesUseCase: SavePreferencesUseCase,
) : ViewModel() {

    private val _uiSate = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiSate.asStateFlow()

    var host by mutableStateOf(TextFieldValue(value = "", status = TextFieldStatus.IDLE))
        private set

    var port by mutableStateOf(TextFieldValue(value = "", status = TextFieldStatus.IDLE))
        private set

    private val hostFormatRegex = """\d{1,3}(\.\d{1,3}){3}""".toRegex()

    private val hostValueRegex =
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
        val port = input.trimStart()

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

        _uiSate.value = UiState.Loading

        viewModelScope.launch {
            testConnectionUseCase(host = host.value, port = port.value.toInt())
                .catch {
                    _uiSate.value = UiState.Error(it)
                }
                .flowOn(Dispatchers.IO)
                .collect { connectionSucceeded ->
                    if (connectionSucceeded) {
                        _uiSate.value = UiState.Success(host.value to port.value)
                    } else {
                        _uiSate.value = UiState.Error(Throwable())
                    }
                }
        }
    }

    suspend fun completeSetup() {
        if (this.host.status != TextFieldStatus.VALID ||
            this.port.status != TextFieldStatus.VALID ||
            this._uiSate.value !is UiState.Success<*>
        ) return

        savePreferencesUseCase.setHost(host.value)
        savePreferencesUseCase.setPort(port.value.toInt())
    }
}
