package com.msa.qiblapro.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R
import com.msa.qiblapro.util.IranCity

@Composable
internal fun MapSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isNearbyActive: Boolean,
    onNearbyToggle: () -> Unit,
    results: List<Pair<IranCity, Double?>>,
    onCityClick: (IranCity) -> Unit,
    isLocationAvailable: Boolean
) {
    val shape = RoundedCornerShape(24.dp)
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.surface.copy(alpha = 0.80f), shape)
            .padding(horizontal = 4.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_city_hint),
                    color = cs.onSurface.copy(alpha = 0.55f)
                )
            },
            leadingIcon = {
                IconButton(onClick = onNearbyToggle, enabled = isLocationAvailable) {
                    Icon(
                        Icons.Default.NearMe,
                        contentDescription = null,
                        tint = if (isNearbyActive) cs.primary else cs.onSurface.copy(alpha = 0.45f)
                    )
                }
            },
            trailingIcon = {
                if (query.isNotEmpty() || isNearbyActive) {
                    IconButton(onClick = {
                        onQueryChange("")
                        if (isNearbyActive) onNearbyToggle()
                    }) {
                        Icon(Icons.Default.Close, null, tint = cs.onSurface.copy(alpha = 0.85f))
                    }
                } else {
                    Icon(
                        Icons.Default.Search,
                        null,
                        tint = cs.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = cs.primary,
                focusedTextColor = cs.onSurface,
                unfocusedTextColor = cs.onSurface
            ),
            singleLine = true
        )

        AnimatedVisibility(visible = results.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .padding(bottom = 8.dp)
            ) {
                items(results) { (city, dist) ->
                    HorizontalDivider(color = cs.onSurface.copy(alpha = 0.08f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCityClick(city) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationCity,
                            null,
                            tint = cs.onSurface.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(city.nameFa, color = cs.onSurface, style = MaterialTheme.typography.bodyLarge)
                            Text(city.provinceFa, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        if (dist != null) {
                            Text(
                                text = String.format("%.1f km", dist),
                                color = cs.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
