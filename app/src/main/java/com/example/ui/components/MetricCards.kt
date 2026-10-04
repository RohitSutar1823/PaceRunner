package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedUnit
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.RecoveryCyan
import com.example.ui.theme.SprintFlame
import java.util.Locale

@Composable
fun WorkoutMetricsGrid(
    elapsedSeconds: Int,
    totalSeconds: Int,
    distanceMeters: Float,
    avgSpeedKmh: Float,
    maxSpeedKmh: Float,
    unit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val distance = if (unit == SpeedUnit.KMH) distanceMeters / 1000f else (distanceMeters / 1000f) * 0.621371f
    val distanceFormatted = String.format(Locale.US, "%.2f", distance)
    val avgSpeed = if (unit == SpeedUnit.KMH) avgSpeedKmh else avgSpeedKmh * 0.621371f
    val maxSpeed = if (unit == SpeedUnit.KMH) maxSpeedKmh else maxSpeedKmh * 0.621371f

    val elapsedMinutes = elapsedSeconds / 60
    val elapsedRemainderSec = elapsedSeconds % 60
    val totalMinutes = totalSeconds / 60
    val timeDisplay = String.format(Locale.US, "%02d:%02d / %02d:00", elapsedMinutes, elapsedRemainderSec, totalMinutes)

    // Rough calorie calc
    val calories = ((avgSpeedKmh * 0.9f) * (elapsedSeconds / 60f) * 0.7f).toInt().coerceAtLeast(0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "TOTAL TIME",
                value = timeDisplay,
                subtitle = "Workout duration",
                icon = Icons.Default.Timer,
                tint = RecoveryCyan,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "DISTANCE",
                value = "$distanceFormatted ${unit.distanceLabel}",
                subtitle = "Total run distance",
                icon = Icons.Default.PinDrop,
                tint = SprintFlame,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "AVG SPEED",
                value = String.format(Locale.US, "%.1f %s", avgSpeed, unit.label),
                subtitle = "Pace overall",
                icon = Icons.Default.ElectricBolt,
                tint = EnergyYellow,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "MAX SPEED",
                value = String.format(Locale.US, "%.1f %s", maxSpeed, unit.label),
                subtitle = "Peak sprint speed",
                icon = Icons.Default.LocalFireDepartment,
                tint = SprintFlame,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LightTextSecondary,
                    letterSpacing = 0.8.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LightTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = LightTextSecondary
            )
        }
    }
}
