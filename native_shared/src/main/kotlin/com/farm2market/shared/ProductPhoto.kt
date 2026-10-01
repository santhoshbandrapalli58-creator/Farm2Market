package com.farm2market.shared

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun ProductPhoto(
    url: String?,
    modifier: Modifier = Modifier,
    background: Color = F2MGreenContainer
) {
    val image = produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, url) {
        value = if (url.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection().apply {
                    connectTimeout = 8_000
                    readTimeout = 8_000
                }
                connection.getInputStream().use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            }.getOrNull()
        }
    }.value

    Box(modifier.background(background), contentAlignment = Alignment.Center) {
        if (image == null) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "Product photo unavailable",
                tint = F2MTextMuted,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Image(image, contentDescription = "Product photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}
