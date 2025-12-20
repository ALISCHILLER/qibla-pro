package com.msa.qiblapro.ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.msa.qiblapro.R

@Composable
fun LuxNeedle(
    rotationDeg: Float,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_needle_lux), // اطمینان حاصل کن که این آیکون وجود دارد
        contentDescription = "Lux Needle",
        modifier = modifier.graphicsLayer {
            rotationZ = rotationDeg
        }
    )
}
