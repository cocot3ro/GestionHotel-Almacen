package com.cocot3ro.gh.almacen.ui.screens.update

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.app_image
import org.jetbrains.compose.resources.painterResource

@Composable
fun UpdateScreen(modifier: Modifier) {

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.app_image),
                contentDescription = null
            )
        }
    }
}