package com.cocot3ro.gh.almacen.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cocot3ro.gh.almacen.domain.model.CargaDescargaMode
import com.cocot3ro.gh.almacen.ui.screens.almacen.AlmacenScreen
import com.cocot3ro.gh.almacen.ui.screens.cargadescarga.CargaDescargaScreen
import com.cocot3ro.gh.almacen.ui.screens.home.HomeScreen
import com.cocot3ro.gh.almacen.ui.screens.login.LoginScreen
import com.cocot3ro.gh.almacen.ui.screens.setup.SetupScreen
import com.cocot3ro.gh.almacen.ui.screens.setup.SetupStep
import com.cocot3ro.gh.almacen.ui.screens.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NavigationWrapper(startDestination: Any) {

    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<Splash> { _ ->
            SplashScreen(
                modifier = Modifier.fillMaxSize(),
                onSetupRequired = {
                    navController.popBackStack()
                    navController.navigate(Setup())
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
                onNavigateToHome = {
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
                    navController.popBackStack()
                    navController.navigate(route = Setup(SetupStep.SERVER))
                }
            )
        }

        composable<Login> { _ ->
            LoginScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateToAlmacen = {
                    navController.navigate(route = Almacen)
                }
            )
        }

        composable<Almacen> { _ ->
            AlmacenScreen(
                modifier = Modifier.fillMaxSize(),
                onNavigateBack = navController::popBackStack,
                onNavigateToCargaDescarga = { cargaDescargaMode: CargaDescargaMode ->
                    navController.navigate(route = CargaDescarga(cargaDescargaMode))
                }
            )
        }

        composable<CargaDescarga> { navBackStack: NavBackStackEntry ->
            val cargaDescargaMode: CargaDescargaMode = navBackStack.toRoute<CargaDescarga>().mode

            @Suppress("UndeclaredKoinUsage")
            CargaDescargaScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = koinViewModel(parameters = { parametersOf(cargaDescargaMode) }),
                onNavigateBack = navController::popBackStack,
                onUnauthorized = {
                    navController.popBackStack(Login, false)
                }
            )
        }
    }
}
