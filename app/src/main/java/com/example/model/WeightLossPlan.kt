package com.example.model

import java.util.Locale
import kotlin.math.roundToInt

data class WeightLossPlan(
    val heightCm: Float,
    val currentWeightKg: Float,
    val targetWeightKg: Float,
    val weightToLoseKg: Float,
    val currentBmi: Float,
    val targetBmi: Float,
    val recommendedFastSpeedKmh: Float,
    val recommendedSlowSpeedKmh: Float,
    val estCaloriesPer10Min: Int,
    val estWeeksToTarget: Int,
    val dailyRunAdvice: String,
    val cardioIntensityLevel: String
) {
    companion object {
        fun calculate(heightCm: Float, currentWeightKg: Float, targetWeightKg: Float): WeightLossPlan {
            val weightDiff = (currentWeightKg - targetWeightKg).coerceAtLeast(0f)
            val heightM = (heightCm / 100f).coerceAtLeast(1.0f)
            val currentBmi = currentWeightKg / (heightM * heightM)
            val targetBmi = targetWeightKg / (heightM * heightM)

            // Adjust recommendations according to weight to lose and BMI
            val (fastSpeed, slowSpeed, intensity) = when {
                currentBmi >= 29f -> {
                    // Joint-safe high-metabolic burn
                    Triple(9.5f, 4.8f, "Low-Impact Fat Burn")
                }
                weightDiff >= 10f -> {
                    // Aggressive Fat Loss HIIT
                    Triple(11.5f, 5.2f, "High-Calorie HIIT Burn")
                }
                weightDiff >= 5f -> {
                    // Moderate Weight Loss
                    Triple(10.5f, 5.0f, "Cardio Fat-Shredding")
                }
                else -> {
                    // Lean Toning & Maintenance
                    Triple(10.0f, 5.0f, "Aerobic Toning")
                }
            }

            // Height-stride adjustment
            val heightAdjustment = if (heightCm > 182f) 0.3f else if (heightCm < 162f) -0.3f else 0f
            val finalFastSpeed = ((fastSpeed + heightAdjustment) * 10).roundToInt() / 10f
            val finalSlowSpeed = ((slowSpeed) * 10).roundToInt() / 10f

            // Estimated calories for 10-minute interval run
            val metAverage = 9.5f
            val calories = (0.0175f * metAverage * currentWeightKg * 10f).roundToInt().coerceAtLeast(70)
            // Estimated weeks: with 10 min daily run (plus EPOC metabolic boost of 35 kcal ~160 kcal/day)
            // Combined with a healthy morning routine (~350 kcal total daily net deficit) = ~0.35 - 0.5 kg fat/week
            val weeks = if (weightDiff <= 0.5f) 1 else ((weightDiff / 0.45f).roundToInt()).coerceIn(1, 52)

            val advice = if (weightDiff > 0.5f) {
                "Run 10 minutes every morning (alternating 1-min fast at ${String.format(Locale.US, "%.1f", finalFastSpeed)} km/h and 1-min slow at ${String.format(Locale.US, "%.1f", finalSlowSpeed)} km/h). Consistency daily will help you shed ${String.format(Locale.US, "%.1f", weightDiff)} kg in about $weeks weeks!"
            } else {
                "You are already at your target weight! Keep running 10 minutes every morning for cardiovascular endurance and maintenance."
            }

            return WeightLossPlan(
                heightCm = heightCm,
                currentWeightKg = currentWeightKg,
                targetWeightKg = targetWeightKg,
                weightToLoseKg = weightDiff,
                currentBmi = currentBmi,
                targetBmi = targetBmi,
                recommendedFastSpeedKmh = finalFastSpeed,
                recommendedSlowSpeedKmh = finalSlowSpeed,
                estCaloriesPer10Min = calories,
                estWeeksToTarget = weeks,
                dailyRunAdvice = advice,
                cardioIntensityLevel = intensity
            )
        }
    }
}
