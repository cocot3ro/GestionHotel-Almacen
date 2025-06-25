package com.cocot3ro.gh.almacen.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cocot3ro.gh.almacen.ui.screens.home.HomeScreen
import com.cocot3ro.gh.almacen.ui.screens.login.LoginScreen
import com.cocot3ro.gh.almacen.ui.screens.main.MainScreen
import com.cocot3ro.gh.almacen.ui.screens.settings.SettingsScreen
import com.cocot3ro.gh.almacen.ui.screens.setup.SetupScreen
import com.cocot3ro.gh.almacen.ui.screens.splash.SplashScreen

@Composable
fun NavigationWrapper(startDestination: Any) {

    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Splash> { _ ->
            SplashScreen(
                modifier = Modifier.fillMaxSize(),
                onSetupRequired = {
                    navController.popBackStack()
                    navController.navigate(Setup)
                },
                onSplashFinished = {
                    navController.popBackStack()
                    navController.navigate(route = Home)
                }
            )
        }

        composable<Setup> { _ ->
            SetupScreen(
                modifier = Modifier.fillMaxSize(),
                onSetupCompleted = {
                    navController.popBackStack()
                    navController.navigate(route = Home)
                }
            )
        }

        composable<Home> { _ ->
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToLogin = {
                    navController.popBackStack()
                    navController.navigate(route = Login)
                },
                onNavigateToSettings = {
                    navController.navigate(route = Settings)
                }
            )
        }

        composable<Login> { _ ->
            LoginScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToMain = {
                    navController.navigate(route = Main)
                }
            )
        }

        composable<Main> { _ ->
            MainScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateBackToLogin = navController::popBackStack
            )
        }

        composable<Settings> { _ ->
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = navController::popBackStack,
                onSettingsChanged = {
                    navController.popBackStack(Main, false)
                }
            )
        }
    }
}
