package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhaseType
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MorningGreen
import com.example.ui.theme.SprintFlame
import com.example.viewmodel.IntervalPattern

@Composable
fun IntervalTimeline(
    currentInterval: Int,
    totalIntervals: Int,
    currentPhase: PhaseType,
    intervalPattern: IntervalPattern,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IntervalPillScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "10-MIN WORKOUT TIMELINE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = LightTextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Interval $currentInterval of $totalIntervals",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = LightTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentPhase == PhaseType.FAST) SprintFlame else MorningGreen)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (currentPhase == PhaseType.FAST) "FAST" else "SLOW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalIntervals) {
                val isCompleted = i < currentInterval
                val isCurrent = i == currentInterval
                val isFastPhase = when (intervalPattern) {
                    IntervalPattern.STANDARD_ALTERNATING -> (i % 2) != 0
                    IntervalPattern.FAST_FAST_FAST_SLOW -> (i % 4) != 0
                }
                val phaseBaseColor = if (isFastPhase) SprintFlame else MorningGreen

                val bgColor: Color = when {
                    isCompleted -> phaseBaseColor
                    isCurrent -> phaseBaseColor
                    else -> DarkBorder
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 12.dp else 7.dp)
                        .then(if (isCurrent) Modifier.scale(scaleY = pulseScale, scaleX = 1f) else Modifier)
                        .clip(RoundedCornerShape(4.dp))
                        .background(bgColor)
                        .then(
                            if (isCurrent) Modifier.border(1.dp, Color.White, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SprintFlame)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Fast (1 min)",
                    fontSize = 11.sp,
                    color = LightTextSecondary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MorningGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Slow (1 min)",
                    fontSize = 11.sp,
                    color = LightTextSecondary
                )
            }
        }
    }
}
