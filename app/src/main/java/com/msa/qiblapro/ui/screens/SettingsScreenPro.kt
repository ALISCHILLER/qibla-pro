package com.msa.qiblapro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.data.settings.AppSettings
import com.msa.qiblapro.ui.pro.GlassCard
import com.msa.qiblapro.ui.pro.ProBackground
import com.msa.qiblapro.ui.pro.proShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenPro(
    modifier: Modifier = Modifier,
    s: AppSettings, // Using the existing AppSettings data class
    onToggleTrueNorth: (Boolean) -> Unit,
    onSmoothingChange: (Float) -> Unit,
    onToleranceChange: (Float) -> Unit,
    onToggleGpsPrompt: (Boolean) -> Unit,
    onMapTypeChange: (Int) -> Unit,
    onToggleIranCities: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit
) {
    ProBackground {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
                    Text("تنظیمات", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "همه چیز رو دقیق و شخصی‌سازی شده تنظیم کن ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                    )
                }
            }

            // Compass Section
            item {
                SectionCard(
                    title = "قطب‌نما",
                    icon = Icons.Filled.CompassCalibration
                ) {
                    ProSwitchRow(
                        title = "شمال واقعی",
                        subtitle = "تصحیح انحراف مغناطیسی برای دقت بیشتر",
                        checked = s.useTrueNorth,
                        onCheckedChange = onToggleTrueNorth
                    )

                    ProSliderRow(
                        title = "نرم‌سازی حرکت عقربه",
                        value = s.smoothing,
                        valueText = "%.2f".format(s.smoothing),
                        range = 0.5f..0.97f,
                        onValueChange = onSmoothingChange
                    )

                    ProSliderRow(
                        title = "حساسیت هم‌ترازی",
                        value = s.alignTolerance.toFloat(),
                        valueText = "${s.alignTolerance}°",
                        range = 1f..15f,
                        onValueChange = onToleranceChange
                    )
                }
            }

            // Alerts Section
            item {
                SectionCard(title = "هشدارها", icon = Icons.Filled.AutoFixHigh) {
                    ProSwitchRow(
                        title = "لرزش هنگام رسیدن به قبله",
                        subtitle = "یک ویبره کوتاه برای تایید جهت",
                        checked = s.enableVibration,
                        onCheckedChange = onVibrationChange
                    )
                     ProSwitchRow(
                        title = "صدای هشدار",
                        subtitle = "یک بیپ کوتاه برای تایید جهت",
                        checked = s.enableSound,
                        onCheckedChange = onSoundChange
                    )
                }
            }

            // Map Section
            item {
                SectionCard(title = "نقشه", icon = Icons.Filled.Map) {
                    ProSwitchRow(
                        title = "نمایش شهرهای ایران",
                        subtitle = "برای انتخاب سریع و تست",
                        checked = s.showIranCities,
                        onCheckedChange = onToggleIranCities
                    )

                    ProSegmentedRow(
                        title = "نوع نقشه",
                        options = listOf("عادی", "ماهواره", "ترکیبی", "ناهمواری"),
                        selectedIndex = s.mapType -1, // mapType is 1-4
                        onSelected = { onMapTypeChange(it + 1) } // Convert back to 1-4
                    )
                }
            }

             // GPS Section
            item {
                SectionCard(title = "GPS", icon = Icons.Filled.BatterySaver) {
                    ProSwitchRow(
                        title = "هشدار فعال‌سازی GPS",
                        subtitle = "برای دقت بهتر پیشنهاد فعال‌سازی نمایش داده شود",
                        checked = s.showGpsPrompt,
                        onCheckedChange = onToggleGpsPrompt
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().proShadow()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ProSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun ProSliderRow(
    title: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Text(title, style = MaterialTheme.typography.bodyLarge)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Slider(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
        Spacer(Modifier.width(10.dp))
        Text(valueText, style = MaterialTheme.typography.labelMedium)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun ProSegmentedRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Text(title, style = MaterialTheme.typography.bodyLarge)
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size)
            ) { Text(label) }
        }
    }
}
