package com.cocot3ro.gh.almacen.ui.navigation

import com.cocot3ro.gh.almacen.domain.model.CargaDescargaMode
import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Setup

@Serializable
object Home

@Serializable
object Almacen

@Serializable
data class CargaDescarga(val mode: CargaDescargaMode)

@Serializable
object Login

@Serializable
object Settings
