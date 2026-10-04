package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhaseType
import com.example.model.SpeedUnit
import com.example.model.WorkoutState
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EnergyYellow
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MorningGreen
import com.example.ui.theme.MorningGreenBright
import com.example.ui.theme.SprintFlame
import java.util.Locale

@Composable
fun PhaseHeroCard(
    phase: PhaseType,
    nextPhase: PhaseType,
    remainingSeconds: Int,
    intervalTotalSeconds: Int,
    workoutState: WorkoutState,
    intervalIndex: Int,
    totalIntervals: Int,
    requiredCurrentSpeedKmh: Float,
    requiredNextSpeedKmh: Float,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val isFast = phase == PhaseType.FAST
    val isNextFast = nextPhase == PhaseType.FAST

    // Fast = Bold Sprint Flame, Slow = Big Morning Green
    val activeColor by animateColorAsState(
        targetValue = if (isFast) SprintFlame else MorningGreen,
        animationSpec = tween(400),
        label = "PhaseColor"
    )

    val nextColor = if (isNextFast) SprintFlame else MorningGreen

    val progress = (remainingSeconds.toFloat() / intervalTotalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "IntervalProgress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val curReqSpeedFormatted = String.format(
        Locale.US,
        "%.1f %s",
        if (speedUnit == SpeedUnit.KMH) requiredCurrentSpeedKmh else requiredCurrentSpeedKmh * 0.621371f,
        speedUnit.label
    )

    val nextReqSpeedFormatted = String.format(
        Locale.US,
        "%.1f %s",
        if (speedUnit == SpeedUnit.KMH) requiredNextSpeedKmh else requiredNextSpeedKmh * 0.621371f,
        speedUnit.label
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DarkSurface)
            .border(
                width = 2.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        activeColor,
                        activeColor.copy(alpha = 0.35f),
                        DarkBorder
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(20.dp)
            .testTag("phase_hero_card"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Indicator: Answers "Should I run fast or slow right now?"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(activeColor.copy(alpha = 0.22f))
                    .border(1.5.dp, activeColor, RoundedCornerShape(50))
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFast) Icons.Default.FlashOn else Icons.Default.SelfImprovement,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFast) "RUN FAST NOW!" else "RUN SLOW NOW!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = activeColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Timer with Circular Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(190.dp),
                    color = DarkBorder,
                    strokeWidth = 13.dp,
                    strokeCap = StrokeCap.Round
                )

                // Active Progress
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(190.dp),
                    color = activeColor,
                    strokeWidth = 13.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Black,
                        color = LightTextPrimary,
                        letterSpacing = (-1.5).sp,
                        modifier = Modifier.testTag("interval_timer_text")
                    )
                    Text(
                        text = "REMAINING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextSecondary,
                        letterSpacing = 1.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Interval $intervalIndex / $totalIntervals",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Required Speed Target for Weight Loss
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, activeColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "REQUIRED SPEED (TARGET)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isFast) "High-Intensity Fat Burn" else "Aerobic Recovery",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightTextPrimary
                        )
                    }
                }

                Text(
                    text = curReqSpeedFormatted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = activeColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "NEXT SPEED & UPCOMING INTERVAL" Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070E1A))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingFlat,
                        contentDescription = null,
                        tint = nextColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEXT (IN $timeFormatted):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isNextFast) "RUN FAST" else "RUN SLOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = nextColor
                    )
                }

                Text(
                    text = "Next: $nextReqSpeedFormatted",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary
                )
            }
        }
    }
}
