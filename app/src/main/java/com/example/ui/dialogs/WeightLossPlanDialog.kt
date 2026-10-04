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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.WeightLossPlan
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
fun WeightLossPlanDialog(
    initialHeightCm: Float,
    initialWeightKg: Float,
    initialTargetWeightKg: Float,
    speedUnit: SpeedUnit,
    onApplyPlan: (heightCm: Float, weightKg: Float, targetKg: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var heightCm by remember { mutableFloatStateOf(initialHeightCm) }
    var currentWeightKg by remember { mutableFloatStateOf(initialWeightKg) }
    var targetWeightKg by remember { mutableFloatStateOf(initialTargetWeightKg) }

    val livePlan = remember(heightCm, currentWeightKg, targetWeightKg) {
        WeightLossPlan.calculate(heightCm, currentWeightKg, targetWeightKg)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .border(2.dp, Brush.verticalGradient(listOf(MorningGreen, SprintFlame)), RoundedCornerShape(26.dp))
                .testTag("weight_loss_plan_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MorningGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = MorningGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WEIGHT LOSS RUNNING PLAN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = LightTextPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Personalized 10-Minute Speeds",
                                fontSize = 11.sp,
                                color = MorningGreenBright
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LightTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Section (Height, Current Weight, Target Weight)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Height Adjuster
                    ParamSlider(
                        label = "Height",
                        value = heightCm,
                        unit = "cm",
                        icon = Icons.Default.Height,
                        color = MorningGreen,
                        range = 140f..210f,
                        onValueChange = { heightCm = it }
                    )

                    // Current Weight
                    ParamSlider(
                        label = "Current Weight",
                        value = currentWeightKg,
                        unit = "kg",
                        icon = Icons.Default.MonitorWeight,
                        color = SprintFlame,
                        range = 45f..160f,
                        onValueChange = { currentWeightKg = it }
                    )

                    // Target Weight
                    ParamSlider(
                        label = "Target Weight",
                        value = targetWeightKg,
                        unit = "kg",
                        icon = Icons.Default.FitnessCenter,
                        color = MorningGreenBright,
                        range = 40f..140f,
                        onValueChange = { targetWeightKg = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Results Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF002914)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(MorningGreen, MorningGreenBright))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WEIGHT REDUCTION GOAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MorningGreenBright
                            )
                            Text(
                                text = String.format(Locale.US, "Lose %.1f kg", livePlan.weightToLoseKg),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = LightTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BMI Change:",
                                fontSize = 12.sp,
                                color = LightTextSecondary
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f  ➔  %.1f", livePlan.currentBmi, livePlan.targetBmi),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estimated Timeline:",
                                fontSize = 12.sp,
                                color = LightTextSecondary
                            )
                            Text(
                                text = "~${livePlan.estWeeksToTarget} weeks with daily 10-min runs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MorningGreen
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Calorie Burn:",
                                fontSize = 12.sp,
                                color = LightTextSecondary
                            )
                            Text(
                                text = "~${livePlan.estCaloriesPer10Min} kcal / 10-min session",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EnergyYellow
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated Recommended Fast and Slow Speeds
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RECOMMENDED RUNNING SPEEDS FOR TARGET WEIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LightTextSecondary,
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Fast Speed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SprintFlame.copy(alpha = 0.15f))
                                .border(1.5.dp, SprintFlame, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "FAST SPRINT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SprintFlame
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f %s", livePlan.recommendedFastSpeedKmh, speedUnit.label),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = "1 Min High Burn",
                                    fontSize = 10.sp,
                                    color = LightTextSecondary
                                )
                            }
                        }

                        // Slow Speed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MorningGreen.copy(alpha = 0.15f))
                                .border(1.5.dp, MorningGreen, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SLOW RECOVERY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MorningGreen
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f %s", livePlan.recommendedSlowSpeedKmh, speedUnit.label),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LightTextPrimary
                                )
                                Text(
                                    text = "1 Min Recovery Jog",
                                    fontSize = 10.sp,
                                    color = LightTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = livePlan.dailyRunAdvice,
                        fontSize = 12.sp,
                        color = LightTextSecondary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Apply Button
                Button(
                    onClick = {
                        onApplyPlan(heightCm, currentWeightKg, targetWeightKg)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("apply_weight_plan_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MorningGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SET FAST & SLOW SPEEDS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ParamSlider(
    label: String,
    value: Float,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightTextSecondary
                )
            }
            Text(
                text = String.format(Locale.US, "%.0f %s", value, unit),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LightTextPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onValueChange((value - 1f).coerceIn(range)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = LightTextSecondary, modifier = Modifier.size(16.dp))
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = DarkBorder
                )
            )

            IconButton(
                onClick = { onValueChange((value + 1f).coerceIn(range)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", tint = LightTextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
