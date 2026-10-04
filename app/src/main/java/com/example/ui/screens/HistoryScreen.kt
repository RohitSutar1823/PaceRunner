package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WorkoutEntity
import com.example.model.SpeedUnit
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.RecoveryCyan
import com.example.ui.theme.SprintFlame
import com.example.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val workouts by viewModel.pastWorkouts.collectAsStateWithLifecycle()
    val totalRuns by viewModel.totalWorkoutsCount.collectAsStateWithLifecycle()
    val totalMeters by viewModel.totalDistanceMeters.collectAsStateWithLifecycle()
    val maxRecordSpeed by viewModel.maxSpeedRecord.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unit = uiState.speedUnit

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WORKOUT HISTORY",
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
                        modifier = Modifier.testTag("history_back_button")
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
            // Lifetime Stats Card
            item {
                LifetimeStatsCard(
                    totalRuns = totalRuns,
                    totalMeters = totalMeters,
                    maxSpeedKmh = maxRecordSpeed,
                    unit = unit
                )
            }

            item {
                Text(
                    text = "PAST RUNS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = LightTextSecondary
                )
            }

            if (workouts.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder()
                }
            } else {
                items(workouts, key = { it.id }) { item ->
                    WorkoutHistoryItemCard(
                        workout = item,
                        unit = unit,
                        onDelete = { viewModel.deleteWorkoutRecord(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LifetimeStatsCard(
    totalRuns: Int,
    totalMeters: Float,
    maxSpeedKmh: Float,
    unit: SpeedUnit
) {
    val totalDist = if (unit == SpeedUnit.KMH) totalMeters / 1000f else (totalMeters / 1000f) * 0.621371f
    val peakSpeed = if (unit == SpeedUnit.KMH) maxSpeedKmh else maxSpeedKmh * 0.621371f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = EnergyYellow,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIFETIME ACHIEVEMENTS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = EnergyYellow
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$totalRuns",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = LightTextPrimary
                    )
                    Text("Total Runs", fontSize = 12.sp, color = LightTextSecondary)
                }

                Column {
                    Text(
                        text = String.format(Locale.US, "%.1f %s", totalDist, unit.distanceLabel),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = RecoveryCyan
                    )
                    Text("Total Distance", fontSize = 12.sp, color = LightTextSecondary)
                }

                Column {
                    Text(
                        text = String.format(Locale.US, "%.1f %s", peakSpeed, unit.label),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SprintFlame
                    )
                    Text("Record Speed", fontSize = 12.sp, color = LightTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItemCard(
    workout: WorkoutEntity,
    unit: SpeedUnit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(workout.timestamp))

    val dist = if (unit == SpeedUnit.KMH) workout.distanceMeters / 1000f else (workout.distanceMeters / 1000f) * 0.621371f
    val avgSpeed = if (unit == SpeedUnit.KMH) workout.avgSpeedKmh else workout.avgSpeedKmh * 0.621371f
    val maxSpeed = if (unit == SpeedUnit.KMH) workout.maxSpeedKmh else workout.maxSpeedKmh * 0.621371f
    val minutes = workout.totalDurationSeconds / 60
    val seconds = workout.totalDurationSeconds % 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${workout.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SprintFlame.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = SprintFlame,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = workout.workoutTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LightTextPrimary
                        )
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.bodySmall,
                            color = LightTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = LightTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = String.format(Locale.US, "%.2f %s", dist, unit.distanceLabel),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextPrimary
                    )
                    Text("Distance", fontSize = 11.sp, color = LightTextSecondary)
                }

                Column {
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RecoveryCyan
                    )
                    Text("Duration", fontSize = 11.sp, color = LightTextSecondary)
                }

                Column {
                    Text(
                        text = String.format(Locale.US, "%.1f %s", avgSpeed, unit.label),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EnergyYellow
                    )
                    Text("Avg Speed", fontSize = 11.sp, color = LightTextSecondary)
                }

                Column {
                    Text(
                        text = "${workout.completedIntervals}/${workout.totalIntervals}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SprintFlame
                    )
                    Text("Intervals", fontSize = 11.sp, color = LightTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = null,
                tint = RecoveryCyan,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No runs logged yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Complete your first 10-minute morning interval workout to see your speed and pace history here!",
                style = MaterialTheme.typography.bodySmall,
                color = LightTextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
