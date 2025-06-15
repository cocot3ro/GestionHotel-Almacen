package com.cocot3ro.gh.almacen.ui.screens.almacen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.add_a_photo_24dp
import gh_almacen.composeapp.generated.resources.hide_image_24dp
import gh_almacen.composeapp.generated.resources.photo_library_24dp
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionBottomSheet(
    onDismissRequest: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onRemoveImage: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        TextButton(
            modifier = Modifier.align(Alignment.End),
            onClick = onDismissRequest
        ) {
            Text(text = "Cancelar")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 24.dp)
        ) {

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onGallery),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    imageVector = vectorResource(Res.drawable.photo_library_24dp),
                    contentDescription = null
                )


                TextButton(onClick = {}) {
                    Text(text = "Galería")
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onCamera),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    imageVector = vectorResource(Res.drawable.add_a_photo_24dp),
                    contentDescription = null
                )

                TextButton(onClick = {}) {
                    Text(text = "Cámara")
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onRemoveImage),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    imageVector = vectorResource(Res.drawable.hide_image_24dp),
                    contentDescription = null
                )

                TextButton(onClick = {}) {
                    Text(text = "Eliminar imagen")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}