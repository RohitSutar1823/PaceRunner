package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SpeedUnit
import com.example.model.WorkoutSummaryData
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.RecoveryCyan
import com.example.ui.theme.SprintFlame
import java.util.Locale

@Composable
fun WorkoutSummaryDialog(
    summary: WorkoutSummaryData,
    unit: SpeedUnit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, Brush.verticalGradient(listOf(SprintFlame, RecoveryCyan)), RoundedCornerShape(28.dp))
                .testTag("workout_summary_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SprintFlame, EnergyYellow))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = "Workout Complete",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WORKOUT CRUSHED!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = LightTextPrimary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Morning interval run saved to your log",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightTextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Key Stat: Distance & Speed
                val distance = if (unit == SpeedUnit.KMH) summary.distanceMeters / 1000f else (summary.distanceMeters / 1000f) * 0.621371f
                val avgSpeed = if (unit == SpeedUnit.KMH) summary.avgSpeedKmh else summary.avgSpeedKmh * 0.621371f
                val maxSpeed = if (unit == SpeedUnit.KMH) summary.maxSpeedKmh else summary.maxSpeedKmh * 0.621371f
                val minutes = summary.totalDurationSeconds / 60
                val seconds = summary.totalDurationSeconds % 60

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceElevated)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryRow(
                        icon = Icons.Default.PinDrop,
                        label = "Distance",
                        value = String.format(Locale.US, "%.2f %s", distance, unit.distanceLabel),
                        color = SprintFlame
                    )
                    SummaryRow(
                        icon = Icons.Default.Timer,
                        label = "Duration",
                        value = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                        color = RecoveryCyan
                    )
                    SummaryRow(
                        icon = Icons.Default.ElectricBolt,
                        label = "Average Speed",
                        value = String.format(Locale.US, "%.1f %s", avgSpeed, unit.label),
                        color = EnergyYellow
                    )
                    SummaryRow(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Max Speed",
                        value = String.format(Locale.US, "%.1f %s", maxSpeed, unit.label),
                        color = SprintFlame
                    )
                    SummaryRow(
                        icon = Icons.Default.Check,
                        label = "Intervals Completed",
                        value = "${summary.completedIntervals} / ${summary.totalIntervals}",
                        color = RecoveryCyan
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("summary_done_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SprintFlame)
                ) {
                    Text(
                        text = "DONE & RETURN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = LightTextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = LightTextPrimary
        )
    }
}
