package com.msa.qiblapro.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R

@Composable
internal fun MapTypeChips(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        1 to stringResource(R.string.map_type_normal),
        2 to stringResource(R.string.map_type_satellite),
        3 to stringResource(R.string.map_type_terrain),
        4 to stringResource(R.string.map_type_hybrid)
    )

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { (id, title) ->
                FilterChip(
                    selected = selected == id,
                    onClick = { onSelect(id) },
                    label = { Text(title) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        selectedContainerColor = Color.White.copy(alpha = 0.20f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
