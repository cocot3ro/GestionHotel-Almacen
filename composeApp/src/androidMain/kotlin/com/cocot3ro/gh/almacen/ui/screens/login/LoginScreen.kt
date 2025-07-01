@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.screens.login

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.domain.model.UserDomain
import com.cocot3ro.gh.almacen.ui.screens.login.ext.getUser
import com.cocot3ro.gh.almacen.ui.state.UiState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier,
    viewModel: LoginViewModel = koinViewModel<LoginViewModel>(),
    onNavigateToMain: () -> Unit
) {
    Scaffold(modifier = modifier) { innerPadding ->
        val uiState: UiState = viewModel.usersState.collectAsStateWithLifecycle().value

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            isRefreshing = uiState is UiState.Reloading<*>,
            onRefresh = viewModel::refreshUsers
        ) {

            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(all = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (uiState) {
                    UiState.Idle -> Unit

                    UiState.Loading -> {
                        items(count = 50) {
                            UserShimmer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                        }
                    }

                    is UiState.Reloading<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val users = uiState.cache as List<UserDomain>

                        if (users.isNotEmpty()) {
                            items(users) { user: UserDomain ->
                                User(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    user = user,
                                    onClick = {}
                                )
                            }
                        } else {
                            stickyHeader {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        modifier = Modifier.align(Alignment.Center),
                                        text = "No hay usuarios registrados"
                                    )
                                }
                            }
                        }
                    }

                    is UiState.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val users = uiState.value as List<UserDomain>

                        if (users.isNotEmpty()) {
                            items(users) { user: UserDomain ->
                                User(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    user = user,
                                    onClick = { viewModel.setUserToLogin(user) }
                                )
                            }
                        } else {
                            stickyHeader {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        modifier = Modifier.align(Alignment.Center),
                                        text = "No hay usuarios registrados"
                                    )
                                }
                            }
                        }
                    }

                    is UiState.Error<*> -> {
                        Log.e("LoginScreen", "Error fetching users", uiState.cause)

                        stickyHeader {
                            var expandError: Boolean by remember { mutableStateOf(false) }
                            val sheetState: SheetState = rememberModalBottomSheetState(
                                skipPartiallyExpanded = false
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(all = 8.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = { expandError = true },
                                        onLongClickLabel = "Expand error message"
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Error de conexión")

                                if (uiState.retry) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp)
                                    )
                                }

                                if (!expandError) return@Column
                                val scrollState: ScrollState = rememberScrollState()

                                ModalBottomSheet(
                                    modifier = Modifier.fillMaxSize(),
                                    sheetState = sheetState,
                                    onDismissRequest = { expandError = false }
                                ) {
                                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                                        Text(text = uiState.cause.message ?: "No message provided")
                                        Text(text = uiState.cause.stackTraceToString())
                                    }
                                }
                            }
                        }

                        @Suppress("UNCHECKED_CAST")
                        val users = uiState.cache as List<UserDomain>

                        if (users.isNotEmpty()) {
                            items(users) { user: UserDomain ->
                                User(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    user = user,
                                    onClick = { viewModel.setUserToLogin(user) }
                                )
                            }
                        } else {
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.align(Alignment.Center),
                                        text = "No hay usuarios registrados"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val loginUiState: LoginUiState by viewModel.loginUiState.collectAsStateWithLifecycle()

        loginUiState.getUser()?.let { user ->
            LoginDialog(
                user = user,
                state = loginUiState,
                onDismissRequest = { viewModel.setUserToLogin(null) },
                password = viewModel.password,
                onPasswordChange = viewModel::updatePassword,
                onLogin = {
                    viewModel.login(user, viewModel.password)
                },
                onSuccess = onNavigateToMain
            )
        }

        if (loginUiState is LoginUiState.Success) {
            viewModel.setUserToLogin(null)
        }
    }
}