package com.cocot3ro.gh.almacen.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.cocot3ro.gh.almacen.domain.model.AlmacenUserDomain
import gh_almacen.composeapp.generated.resources.Res
import gh_almacen.composeapp.generated.resources.broken_image_24dp
import gh_almacen.composeapp.generated.resources.lock_24dp
import org.jetbrains.compose.resources.vectorResource

@Composable
fun User(
    modifier: Modifier,
    user: AlmacenUserDomain,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (user.image != null) {
                SubcomposeAsyncImage(
                    modifier = Modifier.size(100.dp),
                    model = user.image,
                    contentDescription = null
                ) {
                    val state: AsyncImagePainter.State by painter.state.collectAsState()
                    when (state) {
                        AsyncImagePainter.State.Empty -> Unit
                        is AsyncImagePainter.State.Loading -> CircularProgressIndicator()
                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()

                        is AsyncImagePainter.State.Error -> Icon(
                            imageVector = vectorResource(Res.drawable.broken_image_24dp),
                            contentDescription = null
                        )
                    }
                }
            }

            if (!user.requiresPassword) {
                Text(
                    modifier = Modifier,
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                val modId = "lockIcon"
                val text: AnnotatedString = buildAnnotatedString {
                    appendInlineContent(id = modId, alternateText = "[icon]")
                    append(' ')
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