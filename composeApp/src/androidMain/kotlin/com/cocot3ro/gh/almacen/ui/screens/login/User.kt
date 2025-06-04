package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.broken_image_24dp
import gh_almacen.composeapp.generated.resources.lock_24dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (user.image != null) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    model = user.image,
                    contentDescription = null,
                    error = painterResource(Res.drawable.broken_image_24dp)
                )
            }

            if (!user.requiresPassword) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                val modId = "lockIcon"
                val text: AnnotatedString = buildAnnotatedString {
                    appendInlineContent(id = modId, alternateText = "[icon]")
                    append(text = user.name)
                }

                val inlineContent: Map<String, InlineTextContent> = mapOf(
                    pair = Pair(
                        first = modId,
                        second = InlineTextContent(
                            placeholder = Placeholder(
                                width = 20.sp,
                                height = 20.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                            ),
                            children = {
                                Icon(
                                    modifier = Modifier.fillMaxSize(),
                                    imageVector = vectorResource(Res.drawable.lock_24dp),
                                    contentDescription = null
                                )
                            }
                        )
                    )
                )

                Text(
                    text = text,
                    inlineContent = inlineContent,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}