package com.msa.qiblapro.ui.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/* -------------------------------------------------------------------------- */
/*  Shapes                                                                     */
/* -------------------------------------------------------------------------- */

private val AppCardShape = RoundedCornerShape(18.dp)

/* -------------------------------------------------------------------------- */
/*  Background (FLAT – NO GRADIENT – NO GLOW)                                  */
/* -------------------------------------------------------------------------- */

@Composable
fun ProBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // ⬇️ رنگ کاملاً مات، بدون گرادینت
            .background(Color(0xFF07121E))
    ) {
        content()
    }
}

/* -------------------------------------------------------------------------- */
/*  Flat Shadow (Material Default)                                             */
/* -------------------------------------------------------------------------- */

fun Modifier.appShadow(
    elevation: Dp = 6.dp
): Modifier = this

/* -------------------------------------------------------------------------- */
/*  Flat Card (NO GLASS – NO BLUR – NO ALPHA)                                  */
/* -------------------------------------------------------------------------- */

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = AppCardShape,
        colors = CardDefaults.cardColors(
            // ⬇️ کاملاً مات
            containerColor = Color(0xFF0E2236)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
