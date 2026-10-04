package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WorkoutState
import com.example.ui.components.IntervalTimeline
import com.example.ui.components.PhaseHeroCard
import com.example.ui.components.SpeedometerGauge
import com.example.ui.components.WorkoutMetricsGrid
import com.example.ui.dialogs.WeightLossPlanDialog
import com.example.ui.dialogs.WorkoutSummaryDialog
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MorningGreen
import com.example.ui.theme.MorningGreenBright
import com.example.ui.theme.RecoveryCyan
import com.example.ui.theme.SprintFlame
import com.example.viewmodel.IntervalPattern
import com.example.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val liveMetrics by viewModel.liveMetrics.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (!fineLocationGranted && !coarseLocationGranted) {
            viewModel.speedTracker.setSimulationMode(true)
        }
    }

    LaunchedEffect(Unit) {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(MorningGreen, SprintFlame))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PACERUNNER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                ),
                                color = LightTextPrimary
                            )
                            Text(
                                text = "Morning Run • 10 Mins",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MorningGreen
                            )
                        }
                    }
                },
                actions = {
                    // Unit Switcher
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { viewModel.toggleSpeedUnit() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("unit_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.speedUnit.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MorningGreen
                        )
                    }

                    // Voice Coach Toggle
                    IconButton(
                        onClick = { viewModel.toggleVoiceCoach() },
                        modifier = Modifier.testTag("voice_coach_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.voiceCoachEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Voice Coach",
                            tint = if (uiState.voiceCoachEnabled) SprintFlame else LightTextMuted
                        )
                    }

                    // History
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("nav_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = LightTextPrimary
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("nav_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Big Green Morning Exercise & Weight Loss Target Card
            item {
                MorningWeightLossBanner(
                    requiredFastSpeedKmh = uiState.requiredFastSpeedKmh,
                    requiredSlowSpeedKmh = uiState.requiredSlowSpeedKmh,
                    speedUnit = uiState.speedUnit,
                    continuousCoachingSec = uiState.continuousCoachingIntervalSec,
                    intervalPattern = uiState.intervalPattern,
                    onTogglePattern = {
                        val next = if (uiState.intervalPattern == IntervalPattern.STANDARD_ALTERNATING) {
                            IntervalPattern.FAST_FAST_FAST_SLOW
                        } else {
                            IntervalPattern.STANDARD_ALTERNATING
                        }
                        viewModel.setIntervalPattern(next)
                    },
                    onOpenWeightLossPlan = { viewModel.showWeightLossDialog(true) },
                    isIdle = uiState.state == WorkoutState.IDLE
                )
            }

            // Gps / Simulation Mode Bar
            item {
                GpsModePill(
                    isSimulation = uiState.isSimulationMode,
                    hasGpsFix = liveMetrics.hasGpsFix,
                    onToggleSimulation = { viewModel.toggleSimulationMode() }
                )
            }

            // Timeline (10 segments)
            item {
                IntervalTimeline(
                    currentInterval = uiState.currentIntervalIndex,
                    totalIntervals = uiState.totalIntervals,
                    currentPhase = uiState.currentPhase,
                    intervalPattern = uiState.intervalPattern
                )
            }

            // Bold Phase Hero Card (Answers "Should I run fast or slow right now?")
            item {
                PhaseHeroCard(
                    phase = uiState.currentPhase,
                    nextPhase = uiState.nextPhase,
                    remainingSeconds = uiState.intervalRemainingSeconds,
                    intervalTotalSeconds = uiState.intervalDurationSeconds,
                    workoutState = uiState.state,
                    intervalIndex = uiState.currentIntervalIndex,
                    totalIntervals = uiState.totalIntervals,
                    requiredCurrentSpeedKmh = uiState.currentRequiredSpeedKmh,
                    requiredNextSpeedKmh = uiState.nextRequiredSpeedKmh,
                    speedUnit = uiState.speedUnit
                )
            }

            // Speedometer Arc with Live Speed vs Required Target Speed
            item {
                SpeedometerGauge(
                    speedKmh = liveMetrics.currentSpeedKmh,
                    requiredSpeedKmh = uiState.currentRequiredSpeedKmh,
                    unit = uiState.speedUnit,
                    currentPhase = uiState.currentPhase,
                    onSpeakSpeed = { viewModel.checkSpeedNow() }
                )
            }

            // Metrics Grid
            item {
                WorkoutMetricsGrid(
                    elapsedSeconds = uiState.totalElapsedSeconds,
                    totalSeconds = uiState.totalWorkoutDurationSeconds,
                    distanceMeters = liveMetrics.totalDistanceMeters,
                    avgSpeedKmh = liveMetrics.avgSpeedKmh,
                    maxSpeedKmh = liveMetrics.maxSpeedKmh,
                    unit = uiState.speedUnit
                )
            }

            // Controls
            item {
                WorkoutControlDeck(
                    state = uiState.state,
                    onStart = { viewModel.startWorkout() },
                    onPause = { viewModel.pauseWorkout() },
                    onResume = { viewModel.resumeWorkout() },
                    onSkip = { viewModel.skipToNextInterval() },
                    onStop = { viewModel.finishWorkout(earlyFinish = true) }
                )
            }
        }
    }

    if (uiState.showSummaryDialog && uiState.lastCompletedSummary != null) {
        WorkoutSummaryDialog(
            summary = uiState.lastCompletedSummary!!,
            unit = uiState.speedUnit,
            onDismiss = { viewModel.dismissSummaryDialog() }
        )
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
private fun MorningWeightLossBanner(
    requiredFastSpeedKmh: Float,
    requiredSlowSpeedKmh: Float,
    speedUnit: com.example.model.SpeedUnit,
    continuousCoachingSec: Int,
    intervalPattern: IntervalPattern,
    onTogglePattern: () -> Unit,
    onOpenWeightLossPlan: () -> Unit,
    isIdle: Boolean
) {
    val fastFormatted = String.format(
        java.util.Locale.US,
        "%.1f %s",
        if (speedUnit == com.example.model.SpeedUnit.KMH) requiredFastSpeedKmh else requiredFastSpeedKmh * 0.621371f,
        speedUnit.label
    )
    val slowFormatted = String.format(
        java.util.Locale.US,
        "%.1f %s",
        if (speedUnit == com.example.model.SpeedUnit.KMH) requiredSlowSpeedKmh else requiredSlowSpeedKmh * 0.621371f,
        speedUnit.label
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF002914)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(MorningGreen, MorningGreenBright))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MorningGreenBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WEIGHT LOSS TARGET",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MorningGreenBright,
                        letterSpacing = 1.sp
                    )
                }

                // Continuous Coach active pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20))
                        .background(Color(0xFF004D25))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MorningGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live Coach Every ${continuousCoachingSec}s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MorningGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fast Target: $fastFormatted",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = SprintFlame
                    )
                    Text(
                        text = "Slow Target: $slowFormatted",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MorningGreen
                    )
                }

                if (isIdle) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF004D25))
                            .border(1.dp, MorningGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onTogglePattern() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pattern: ${if (intervalPattern == IntervalPattern.STANDARD_ALTERNATING) "1m Fast / 1m Slow" else "Fast, Fast, Fast, Slow"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextPrimary
                            )
                            Text(
                                text = "Tap to switch",
                                fontSize = 9.sp,
                                color = MorningGreenBright
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Button to open Personalized Weight Loss Calculator & Plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MorningGreenBright.copy(alpha = 0.15f))
                    .border(1.dp, MorningGreenBright.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { onOpenWeightLossPlan() }
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .testTag("open_weight_loss_calc_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MorningGreenBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Personalize Speeds by Height & Target Weight",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MorningGreenBright
                    )
                }
            }
        }
    }
}

@Composable
private fun GpsModePill(
    isSimulation: Boolean,
    hasGpsFix: Boolean,
    onToggleSimulation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onToggleSimulation() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSimulation) EnergyYellow else if (hasGpsFix) MorningGreen else SprintFlame)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSimulation) "Indoor Treadmill / Simulation Mode" else if (hasGpsFix) "GPS Locked (Outdoor Run)" else "Acquiring GPS Signal...",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = LightTextPrimary
            )
        }

        Text(
            text = if (isSimulation) "Switch to GPS" else "Indoor Mode",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MorningGreen
        )
    }
}

@Composable
private fun WorkoutControlDeck(
    state: WorkoutState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state) {
            WorkoutState.IDLE, WorkoutState.COMPLETED -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("start_workout_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SprintFlame)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Run",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START 10-MIN RUN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            WorkoutState.RUNNING -> {
                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("skip_interval_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        tint = MorningGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Skip", fontWeight = FontWeight.Bold, color = LightTextPrimary)
                }

                Button(
                    onClick = onPause,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(58.dp)
                        .testTag("pause_workout_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EnergyYellow)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PAUSE", fontWeight = FontWeight.Black, color = Color.Black)
                }

                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .testTag("stop_workout_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = SprintFlame,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("End", fontWeight = FontWeight.Bold, color = LightTextPrimary)
                }
            }

            WorkoutState.PAUSED -> {
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .weight(2f)
                        .height(58.dp)
                        .testTag("resume_workout_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MorningGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESUME", fontWeight = FontWeight.Black, color = Color.Black)
                }

                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(58.dp)
                        .testTag("end_paused_workout_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Finish Early",
                        tint = SprintFlame,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finish", fontWeight = FontWeight.Bold, color = LightTextPrimary)
                }
            }
        }
    }
}
