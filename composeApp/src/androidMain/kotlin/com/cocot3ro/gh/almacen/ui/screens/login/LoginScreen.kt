package com.cocot3ro.gh.almacen.ui.screens.login

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import com.cocot3ro.gh.almacen.ui.state.UiState
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier,
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToAlmacen: () -> Unit
) {
    Scaffold(modifier = modifier) { innerPadding ->

        val coroutineScope: CoroutineScope = rememberCoroutineScope()

        val uiState: UiState = viewModel.usersState.collectAsStateWithLifecycle().value

        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            isRefreshing = (uiState is UiState.Loading && !uiState.firstLoad),
            onRefresh = viewModel::refreshUsers
        ) {

            val scrollState: ScrollState = rememberScrollState()

            when {
                uiState is UiState.Idle -> Unit

                uiState is UiState.Loading && !uiState.firstLoad -> {
                    LinearProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState is UiState.Success<*> -> {
                    val users: List<AlmacenUserDomain> =
                        (uiState.value as List<*>).map { it as AlmacenUserDomain }

                    if (users.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(state = scrollState),
                        ) {
                            Text(
                                text = "No users found",
                                fontSize = 24.sp
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            columns = GridCells.FixedSize(120.dp),
                            contentPadding = PaddingValues(all = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(users) { user ->
                                User(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    user = user,
                                    onClick = {
                                        viewModel.setUserToLogin(user)
                                    }
                                )
                            }
                        }

                        val loginUiState: LoginUiState = viewModel.loginUiState
                            .collectAsStateWithLifecycle().value

                        val userForLogin: AlmacenUserDomain? = when (loginUiState) {
                            LoginUiState.Idle -> null
                            is LoginUiState.Waiting -> loginUiState.user
                            is LoginUiState.Loading -> loginUiState.user
                            is LoginUiState.Success -> null
                            is LoginUiState.Fail -> loginUiState.user
                            is LoginUiState.Error -> loginUiState.user
                        }

                        userForLogin?.let { user ->
                            LoginDialog(
                                user = user,
                                state = loginUiState,
                                onDismissRequest = { viewModel.setUserToLogin(null) },
                                password = viewModel.password,
                                onPasswordChange = viewModel::updatePassword,
                                onLogin = {
                                    viewModel.login(user, viewModel.password)
                                }
                            )
                        }

                        if (loginUiState is LoginUiState.Success) {
                            viewModel.setUserToLogin(null)
                            viewModel.updatePassword("")
                            onNavigateToAlmacen()
                        }
                    }
                }

                uiState is UiState.Error<*> -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(state = scrollState),
                    ) {
                        Text(
                            text = "Error",
                            fontSize = 24.sp,
                            color = Color.Red
                        )
                    }

                    Log.e("LoginScreen", "Error fetching users", uiState.cause)
                }
            }
        }
    }
}