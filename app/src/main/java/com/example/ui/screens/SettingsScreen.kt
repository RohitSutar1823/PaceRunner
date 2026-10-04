package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.WeightLossPlanDialog
import com.example.model.SpeedUnit
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MorningGreen
import com.example.ui.theme.MorningGreenBright
import com.example.ui.theme.SprintFlame
import com.example.viewmodel.IntervalPattern
import com.example.viewmodel.WorkoutViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WORKOUT & TARGET SPEED",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = LightTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LightTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = LightTextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Weight Loss Target Speeds
            item {
                SettingsSectionTitle("WEIGHT LOSS TARGET SPEEDS")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Set your required speeds. The voice coach continuously monitors your live pace against these targets!",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextSecondary
                        )

                        // Required Fast Speed Adjuster
                        SpeedAdjusterRow(
                            label = "Required Fast Speed (Sprint)",
                            speedKmh = uiState.requiredFastSpeedKmh,
                            unit = uiState.speedUnit,
                            accentColor = SprintFlame,
                            onSpeedChanged = { newSpeed ->
                                viewModel.setRequiredSpeeds(
                                    fastSpeedKmh = newSpeed,
                                    slowSpeedKmh = uiState.requiredSlowSpeedKmh
                                )
                            }
                        )

                        // Required Slow Speed Adjuster
                        SpeedAdjusterRow(
                            label = "Required Slow Speed (Recovery)",
                            speedKmh = uiState.requiredSlowSpeedKmh,
                            unit = uiState.speedUnit,
                            accentColor = MorningGreen,
                            onSpeedChanged = { newSpeed ->
                                viewModel.setRequiredSpeeds(
                                    fastSpeedKmh = uiState.requiredFastSpeedKmh,
                                    slowSpeedKmh = newSpeed
                                )
                            }
                        )

                        // Open Calculator Dialog Button
                        Button(
                            onClick = { viewModel.showWeightLossDialog(true) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MorningGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personalize by Height & Target Weight",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Interval Pattern Sequence
            item {
                SettingsSectionTitle("INTERVAL RUNNING PATTERN")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        IntervalPattern.entries.forEach { pattern ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setIntervalPattern(pattern) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.intervalPattern == pattern,
                                    onClick = { viewModel.setIntervalPattern(pattern) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MorningGreen,
                                        unselectedColor = LightTextSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = pattern.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (uiState.intervalPattern == pattern) MorningGreenBright else LightTextPrimary
                                    )
                                    Text(
                                        text = pattern.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Continuous Voice Coach Frequency
            item {
                SettingsSectionTitle("LIVE CONTINUOUS VOICE COACHING")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Continuous Live Speed Feedback",
                            subtitle = "Checks live speed and announces \"You are running slow/fast\" continuously",
                            checked = uiState.continuousCoachingEnabled,
                            onCheckedChange = { viewModel.setContinuousCoachingEnabled(it) }
                        )

                        if (uiState.continuousCoachingEnabled) {
                            Text(
                                text = "Coaching Interval: Every ${uiState.continuousCoachingIntervalSec} seconds",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MorningGreen
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 15, 20, 30).forEach { sec ->
                                    val isSelected = uiState.continuousCoachingIntervalSec == sec
                                    Button(
                                        onClick = { viewModel.setContinuousCoachingInterval(sec) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MorningGreen else DarkSurfaceElevated
                                        )
                                    ) {
                                        Text(
                                            text = "${sec}s",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else LightTextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Test Voice Button
                        Button(
                            onClick = {
                                viewModel.voiceCoach.announceLiveSpeedAssessment(
                                    currentSpeedKmh = 7.5f,
                                    requiredSpeedKmh = uiState.requiredFastSpeedKmh,
                                    isFastPhase = true,
                                    unit = uiState.speedUnit
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                tint = MorningGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Voice Coach Response",
                                color = LightTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Duration & Sensor Settings
            item {
                SettingsSectionTitle("WORKOUT DURATION & MODE")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ToggleRow(
                            icon = Icons.Default.DirectionsRun,
                            title = "Indoor Treadmill Mode",
                            subtitle = "Simulates realistic speed when indoors or testing",
                            checked = uiState.isSimulationMode,
                            onCheckedChange = { viewModel.toggleSimulationMode() }
                        )

                        // Unit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Speed Units",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = if (uiState.speedUnit == SpeedUnit.KMH) "Kilometers per hour (km/h)" else "Miles per hour (mph)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LightTextSecondary
                                )
                            }

                            Button(
                                onClick = { viewModel.toggleSpeedUnit() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                            ) {
                                Text(
                                    text = uiState.speedUnit.label.uppercase(),
                                    color = MorningGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showWeightLossDialog) {
        WeightLossPlanDialog(
            initialHeightCm = uiState.userHeightCm,
            initialWeightKg = uiState.userWeightKg,
            initialTargetWeightKg = uiState.targetWeightKg,
            speedUnit = uiState.speedUnit,
            onApplyPlan = { heightCm, currentWeightKg, targetWeightKg ->
                viewModel.applyWeightLossTargetPlan(heightCm, currentWeightKg, targetWeightKg)
            },
            onDismiss = { viewModel.showWeightLossDialog(false) }
        )
    }
}

@Composable
private fun SpeedAdjusterRow(
    label: String,
    speedKmh: Float,
    unit: SpeedUnit,
    accentColor: Color,
    onSpeedChanged: (Float) -> Unit
) {
    val displayedSpeed = if (unit == SpeedUnit.KMH) speedKmh else speedKmh * 0.621371f
    val formatted = String.format(Locale.US, "%.1f %s", displayedSpeed, unit.label)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = LightTextPrimary
            )
            Text(
                text = formatted,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSpeedChanged((speedKmh - 0.5f).coerceAtLeast(2.0f)) },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = LightTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Slider(
                value = speedKmh,
                onValueChange = onSpeedChanged,
                valueRange = 3.0f..18.0f,
                steps = 29,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = DarkBorder
                )
            )

            IconButton(
                onClick = { onSpeedChanged((speedKmh + 0.5f).coerceAtMost(22.0f)) },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = LightTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = MorningGreen,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MorningGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = LightTextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LightTextSecondary
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MorningGreen,
                uncheckedThumbColor = LightTextSecondary,
                uncheckedTrackColor = DarkBorder
            )
        )
    }
}
