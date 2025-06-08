package com.cocot3ro.gh.almacen.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cocot3ro.gh.almacen.ui.navigation.MainDestination
import com.cocot3ro.gh.almacen.ui.navigation.MainNavGraph
import org.jetbrains.compose.resources.vectorResource

@Composable
fun MainScreen(
    modifier: Modifier,
    onNavigateBackToLogin: () -> Unit
) {

    val navController: NavHostController = rememberNavController()
    val startDestination: Any = MainDestination.ALMACEN.route
    var selectedDestination: Int by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        MainNavGraph(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            navController = navController,
            startDestination = startDestination,
            onNavigateBackToLogin = onNavigateBackToLogin
        )

        NavigationBar(modifier = Modifier.fillMaxWidth()) {
            MainDestination.entries.forEachIndexed { index, destination ->
                NavigationBarItem(
                    selected = selectedDestination == index,
                    onClick = {
                        navController.navigate(route = destination.route)
                        selectedDestination = index
                    },
                    icon = {
                        Icon(
                            imageVector = vectorResource(destination.icon),
                            contentDescription = destination.contentDescription
                        )
                    },
                    label = {
                        Text(destination.label)
                    }
                )
            }
        }
    }
}
