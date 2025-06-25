package com.cocot3ro.gh.almacen.ui.navigation

import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.category_24dp
import gh_almacen.composeapp.generated.resources.delivery_truck_speed_24dp
import gh_almacen.composeapp.generated.resources.trolley_24dp
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource

@Serializable
object Splash

@Serializable
object Setup

@Serializable
object Home

@Serializable
object Main

@Serializable
object Almacen

@Serializable
object Carrito

@Serializable
object Camion

@Serializable
object Login

@Serializable
object Settings

enum class MainDestination(
    val route: Any,
    val label: String,
    val icon: DrawableResource,
    val contentDescription: String
) {
    ALMACEN(
        route = Almacen,
        label = "Almacén",
        icon = Res.drawable.category_24dp,
        contentDescription = "Almacen"
    ),
    CARRITO(
        route = Carrito,
        label = "Carrito",
        icon = Res.drawable.trolley_24dp,
        contentDescription = "Carrito"
    ),
    CAMION(
        route = Camion,
        label = "Camión",
        icon = Res.drawable.delivery_truck_speed_24dp,
        contentDescription = "Camion"
    )
}