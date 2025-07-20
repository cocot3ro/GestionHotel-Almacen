package com.cocot3ro.gh.almacen.ui.navigation

import com.cocot3ro.gh.almacen.domain.model.CargaDescargaMode
import com.cocot3ro.gh.almacen.ui.screens.setup.SetupStep
import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
data class Setup(val setupStep: SetupStep? = null)

@Serializable
object Home

@Serializable
object Almacen

@Serializable
data class CargaDescarga(val mode: CargaDescargaMode)

@Serializable
object Login
