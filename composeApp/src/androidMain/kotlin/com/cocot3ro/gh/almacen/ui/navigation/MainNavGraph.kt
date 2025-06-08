package com.cocot3ro.gh.almacen.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cocot3ro.gh.almacen.ui.screens.almacen.AlmacenScreen

@Composable
fun MainNavGraph(
    modifier: Modifier,
    navController: NavHostController,
    startDestination: Any,
    onNavigateBackToLogin: () -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        MainDestination.entries.forEach { destination: MainDestination ->
            when (destination) {
                MainDestination.ALMACEN -> {
                    composable<Almacen> {
                        AlmacenScreen(
                            modifier = Modifier.fillMaxSize(),
                            onNavigateBack = onNavigateBackToLogin,
                        )
                    }
                }

                MainDestination.CARRITO -> {
                    composable<Carrito> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Carrtio Screen")
                        }
                    }
                }

                MainDestination.CAMION -> {
                    composable<Camion> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Camion Screen")
                        }
                    }
                }
            }
        }
    }
}
