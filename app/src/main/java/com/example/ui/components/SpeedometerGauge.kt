package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhaseType
import com.example.model.SpeedUnit
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MorningGreen
import com.example.ui.theme.MorningGreenBright
import com.example.ui.theme.RecoveryCyan
import com.example.ui.theme.SprintFlame
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    speedKmh: Float,
    requiredSpeedKmh: Float,
    unit: SpeedUnit,
    currentPhase: PhaseType,
    onSpeakSpeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedSpeed = if (unit == SpeedUnit.KMH) speedKmh else speedKmh * 0.621371f
    val displayedRequired = if (unit == SpeedUnit.KMH) requiredSpeedKmh else requiredSpeedKmh * 0.621371f

    val isFast = currentPhase == PhaseType.FAST
    val isRunningSlowWhenShouldBeFast = isFast && speedKmh < (requiredSpeedKmh - 1.0f)
    val isRunningFastWhenShouldBeSlow = !isFast && speedKmh > (requiredSpeedKmh + 1.8f)

    val maxScale = if (unit == SpeedUnit.KMH) 25f else 16f
    val fraction = (displayedSpeed / maxScale).coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 350),
        label = "SpeedGaugeAnimation"
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            isRunningSlowWhenShouldBeFast -> SprintFlame
            isRunningFastWhenShouldBeSlow -> EnergyYellow
            isFast -> SprintFlame
            else -> MorningGreen
        },
        label = "StatusColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speedometer_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(DarkBorder, statusColor.copy(alpha = 0.5f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed Icon",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE SPEED TRACKER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = LightTextSecondary
                    )
                }

                // "Check Speed & Target" Action Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.18f))
                        .clickable { onSpeakSpeed() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("speak_speed_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak Speed",
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Check Speed Live",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gauge Canvas
            Box(
                modifier = Modifier.size(220.dp, 125.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp, 115.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = Size(size.width, size.height * 1.8f)
                    val topLeft = Offset(0f, 0f)

                    // Track
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Sweep
                    val activeSweep = 180f * animatedFraction
                    if (activeSweep > 0f) {
                        drawArc(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MorningGreen,
                                    if (isFast) SprintFlame else EnergyYellow
                                )
                            ),
                            startAngle = 180f,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Ticks
                    val cx = size.width / 2f
                    val cy = size.height * 0.9f
                    val r = (size.width / 2f) - (strokeWidth / 2f)
                    for (i in 0..4) {
                        val angleDeg = 180f + (i * 45f)
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val p1 = Offset(
                            (cx + (r - 12.dp.toPx()) * cos(angleRad)).toFloat(),
                            (cy + (r - 12.dp.toPx()) * sin(angleRad)).toFloat()
                        )
                        val p2 = Offset(
                            (cx + (r - 4.dp.toPx()) * cos(angleRad)).toFloat(),
                            (cy + (r - 4.dp.toPx()) * sin(angleRad)).toFloat()
                        )
                        drawLine(
                            color = Color(0xFF475569),
                            start = p1,
                            end = p2,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Speed Text
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", displayedSpeed),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LightTextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = unit.label.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Speed vs Target Assessment Banner
            val assessmentText: String
            val bannerBg: Color
            val bannerBorder: Color

            when {
                displayedSpeed < 0.5f -> {
                    assessmentText = "👟 STATIONARY • Start running!"
                    bannerBg = DarkSurfaceElevated
                    bannerBorder = DarkBorder
                }
                isRunningSlowWhenShouldBeFast -> {
                    val diff = displayedRequired - displayedSpeed
                    assessmentText = "⚠️ RUNNING SLOW! (${String.format(Locale.US, "-%.1f %s", diff, unit.label)}) • RUN FAST!"
                    bannerBg = SprintFlame.copy(alpha = 0.15f)
                    bannerBorder = SprintFlame
                }
                isFast -> {
                    assessmentText = "🔥 RUNNING FAST! Target Met (${String.format(Locale.US, "%.1f", displayedRequired)} ${unit.label})"
                    bannerBg = MorningGreen.copy(alpha = 0.15f)
                    bannerBorder = MorningGreen
                }
                isRunningFastWhenShouldBeSlow -> {
                    assessmentText = "⚠️ RUNNING TOO FAST! Slow down to ${String.format(Locale.US, "%.1f", displayedRequired)} ${unit.label} to recover"
                    bannerBg = EnergyYellow.copy(alpha = 0.15f)
                    bannerBorder = EnergyYellow
                }
                else -> {
                    assessmentText = "💧 RUNNING SLOW • Optimal fat-burn recovery pace"
                    bannerBg = MorningGreen.copy(alpha = 0.15f)
                    bannerBorder = MorningGreen
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bannerBg)
                    .border(1.dp, bannerBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = assessmentText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary
                )
            }
        }
    }
}
