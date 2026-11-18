package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Custom Settings Icon displaying the user-provided design:
 * An industrial dark-navy gear with 8 cog teeth, golden-amber background,
 * and a fireworks cracker with fuse and white spark inside the center circle.
 */
@Composable
fun CrackerGearIcon(
    modifier: Modifier = Modifier.size(38.dp)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_settings_logo),
            contentDescription = "Settings",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
    }
}
