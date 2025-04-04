package com.cocot3ro.gh.almacen.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cocot3ro.gh.almacen.ui.screens.scanner.BarcodeExample
import com.cocot3ro.gh.almacen.ui.screens.setup.SetupScreen
import com.cocot3ro.gh.almacen.ui.screens.splash.SplashScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Splash) {
        composable<Splash> { _ ->
            SplashScreen(
                modifier = Modifier.fillMaxSize(),
                onSetupRequired = {
                    navController.popBackStack()
                    navController.navigate(Setup)
                },
                onSplashFinished = {
                    navController.popBackStack()
                    navController.navigate(Home)
                }
            )
        }

        composable<Setup> { _ ->
            SetupScreen(
                modifier = Modifier.fillMaxSize(),
                onSetupCompleted = {
                    navController.popBackStack()
                    navController.navigate(Home)
                }
            )
        }

        composable<Home> { _ ->
            BarcodeExample(modifier = Modifier.fillMaxSize())
        }
    }
}