package com.cocot3ro.gh.almacen

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.ui.main.MainViewModel
import com.cocot3ro.gh.almacen.ui.navigation.Home
import com.cocot3ro.gh.almacen.ui.navigation.NavigationWrapper
import com.cocot3ro.gh.almacen.ui.navigation.Setup
import com.cocot3ro.gh.almacen.ui.navigation.Splash
import com.cocot3ro.gh.almacen.ui.screens.splash.SplashViewModel
import com.cocot3ro.gh.almacen.ui.state.UiState
import com.cocot3ro.gh.almacen.ui.theme.GhAlmacenTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.KoinContext

// TODO: Check for updates
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()
    private val splashViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            // Keep the splash while uiState is Idle or Loading
            splashScreen.setKeepOnScreenCondition {
                val uiState = splashViewModel.uiState.value
                uiState is UiState.Idle || uiState is UiState.Loading
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KoinContext {
                GhAlmacenTheme {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val uiState = splashViewModel.uiState.collectAsStateWithLifecycle().value
                        when (uiState) {
                            is UiState.Success<*> -> {
                                val requiresSetup: Boolean = uiState.value as Boolean
                                val startDestination: Any = if (requiresSetup) Setup else Home
                                NavigationWrapper(startDestination = startDestination)
                            }

                            // Wait for the splash screen to finish to show any content
                            else -> Unit
                        }
                    } else {
                        // For Android versions below S (12 API 31), use the custom SplashScreen
                        NavigationWrapper(startDestination = Splash)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        runBlocking(Dispatchers.IO) {
            viewModel.logOut()
        }
    }
}
