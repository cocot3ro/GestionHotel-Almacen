package com.cocot3ro.gh.almacen.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.app_image
import org.jetbrains.compose.resources.painterResource

@Composable
fun SplashScreen(modifier: Modifier) {
    Box(modifier = modifier) {
        Image(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(Res.drawable.app_image),
            contentDescription = null
        )
    }
}