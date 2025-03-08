package com.cocot3ro.gh_almacen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GH-Almacen",
    ) {
        App()
    }
}