package com.cocot3ro.gh.almacen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.cocot3ro.gh.almacen.di.initKoin
import com.cocot3ro.gh.almacen.ui.navigation.NavigationWrapper
import com.cocot3ro.gh.almacen.ui.theme.GhAlmacenTheme
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

fun main() = application {

    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
    ) {
        GhAlmacenTheme {
            NavigationWrapper()
        }
    }
}