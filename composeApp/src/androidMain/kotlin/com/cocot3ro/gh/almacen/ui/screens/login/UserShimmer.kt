package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cocot3ro.gh.almacen.ui.ext.shimmerEffect

@Composable
@Preview
fun UserShimmerPreview() {
    UserShimmer(
        modifier = Modifier
            .height(150.dp)
            .width(130.dp)
    )
}

@Composable
fun UserShimmer(modifier: Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .width(100.dp)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .shimmerEffect()
            )

            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
                    .shimmerEffect()
            )
        }
    }
}