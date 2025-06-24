package com.cocot3ro.gh.almacen.ui.screens.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cocot3ro.gh.almacen.ui.navigation.MainDestination
import com.cocot3ro.gh.almacen.ui.navigation.MainNavGraph

@Composable
fun MainScreen(
    modifier: Modifier,
    onNavigateBackToLogin: () -> Unit
) {

    val navController: NavHostController = rememberNavController()
    val startDestination: Any = MainDestination.ALMACEN.route
//    var selectedDestination: Int by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
//        bottomBar = bottomBar@{
//            if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_PORTRAIT)
//                return@bottomBar
//
//            NavigationBar(modifier = Modifier.fillMaxWidth()) {
//                MainDestination.entries.forEachIndexed { index, destination ->
//                    NavigationBarItem(
//                        selected = selectedDestination == index,
//                        onClick = {
//                            navController.navigate(route = destination.route)
//                            selectedDestination = index
//                        },
//                        icon = {
//                            Icon(
//                                imageVector = vectorResource(destination.icon),
//                                contentDescription = destination.contentDescription
//                            )
//                        },
//                        label = {
//                            Text(destination.label)
//                        }
//                    )
//                }
//            }
//        }
    ) { innerPadding ->

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
//            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                NavigationRail {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .padding(start = 4.dp),
//                        verticalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        MainDestination.entries.forEachIndexed { index, destination ->
//                            NavigationRailItem(
//                                selected = selectedDestination == index,
//                                onClick = {
//                                    navController.navigate(route = destination.route)
//                                    selectedDestination = index
//                                },
//                                icon = {
//                                    Icon(
//                                        imageVector = vectorResource(destination.icon),
//                                        contentDescription = destination.contentDescription
//                                    )
//                                },
//                                label = {
//                                    Text(destination.label)
//                                }
//                            )
//                        }
//                    }
//                }
//            }

            MainNavGraph(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                navController = navController,
                startDestination = startDestination,
                onNavigateBackToLogin = onNavigateBackToLogin
            )
        }
    }
}
