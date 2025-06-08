package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemShimmer(modifier: Modifier) {
    Card(modifier = modifier) {
        Text("Loading...", modifier = Modifier.padding(all = 8.dp))
    }
}