package com.msa.qiblapro.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.msa.qiblapro.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val lastPage = 2

    val title = when (page) {
        0 -> stringResource(R.string.onb_title_location)
        1 -> stringResource(R.string.onb_title_calibration)
        else -> stringResource(R.string.onb_title_map)
    }

    val body = when (page) {
        0 -> stringResource(R.string.onb_body_location)
        1 -> stringResource(R.string.onb_body_calibration)
        else -> stringResource(R.string.onb_body_map)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.onb_title)) },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text(stringResource(R.string.onb_skip))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.onb_tip),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (page) {
                                0 -> stringResource(R.string.onb_tip_location)
                                1 -> stringResource(R.string.onb_tip_calibration)
                                else -> stringResource(R.string.onb_tip_map)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(lastPage + 1) { i ->
                        val color = if (i == page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        Surface(
                            shape = CircleShape,
                            color = color,
                            modifier = Modifier.size(if (i == page) 24.dp else 8.dp, 8.dp)
                        ) {}
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (page > 0) {
                        OutlinedButton(onClick = { page-- }) {
                            Text(stringResource(R.string.onb_back))
                        }
                    }
                    Button(
                        onClick = {
                            if (page < lastPage) page++ else onFinish()
                        }
                    ) {
                        Text(if (page < lastPage) stringResource(R.string.onb_next) else stringResource(R.string.onb_start))
                    }
                }
            }
        }
    }
}
