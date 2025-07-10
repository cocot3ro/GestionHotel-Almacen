@file:Suppress("UndeclaredKoinUsage")

package com.cocot3ro.gh.almacen.ui.activity.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocot3ro.gh.almacen.ui.navigation.Home
import com.cocot3ro.gh.almacen.ui.navigation.NavigationWrapper
import com.cocot3ro.gh.almacen.ui.navigation.Setup
import com.cocot3ro.gh.almacen.ui.navigation.Splash
import com.cocot3ro.gh.almacen.domain.state.SplashUiState
import com.cocot3ro.gh.almacen.ui.screens.splash.SplashViewModel
import com.cocot3ro.gh.almacen.ui.theme.GhAlmacenTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            // Keep the splash while uiState is Idle or Loading
            splashScreen.setKeepOnScreenCondition {
                val uiState = splashViewModel.uiState.value
                uiState is SplashUiState.Idle || uiState is SplashUiState.Loading
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GhAlmacenTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val uiState: SplashUiState by splashViewModel.uiState.collectAsStateWithLifecycle()
                    when (uiState) {
                        is SplashUiState.SetupRequired -> {
                            NavigationWrapper(startDestination = Setup)
                        }

                        is SplashUiState.UpdateRequired -> {
                            NavigationWrapper(startDestination = Splash)
                        }

                        is SplashUiState.SplashUiFinished -> {
                            NavigationWrapper(startDestination = Home)
                        }

                        // Wait for the splash screen to finish to show any content
                        else -> Unit
                    }
                } else {
//                     For Android versions below S (12 API 31), use the custom SplashScreen
                    NavigationWrapper(startDestination = Splash)
                }
            }
        }
    }

}
