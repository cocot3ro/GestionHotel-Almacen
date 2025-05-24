package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain

@Composable
fun User(
    modifier: Modifier,
    user: AlmacenUserDomain,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
//        colors = CardDefaults.cardColors(
//            containerColor = Color(red = 0.6f, green = 0.7f, blue = 0.9f)
//        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 0.85f)
        ) {
            if (user.image != null) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    model = user.image,
                    contentDescription = null
                )

                Text(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.BottomCenter),
                    text = user.name,
                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
                )
            } else {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center),
                    text = user.name,
                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
                )
            }
        }
    }
}