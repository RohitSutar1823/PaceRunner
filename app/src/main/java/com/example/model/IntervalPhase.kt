package com.example.model

enum class PhaseType(val label: String, val instruction: String) {
    FAST("RUN FAST", "Sprint now! High intensity!"),
    SLOW("RUN SLOW", "Slow down! Recovery jog.")
}

enum class SpeedUnit(val label: String, val speedFactor: Float, val distanceLabel: String) {
    KMH("km/h", 3.6f, "km"),
    MPH("mph", 2.23694f, "mi")
}

data class IntervalSegment(
    val intervalNumber: Int,
    val type: PhaseType,
    val durationSeconds: Int = 60
)

enum class WorkoutState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class WorkoutSummaryData(
    val totalDurationSeconds: Int,
    val distanceMeters: Float,
    val avgSpeedKmh: Float,
    val maxSpeedKmh: Float,
    val completedIntervals: Int,
    val totalIntervals: Int,
    val calories: Int
)
