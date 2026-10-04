package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.model.PhaseType
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.random.Random

data class LiveMetrics(
    val currentSpeedKmh: Float = 0f,
    val avgSpeedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val totalDistanceMeters: Float = 0f,
    val paceSecondsPerKm: Int = 0,
    val isGpsActive: Boolean = false,
    val hasGpsFix: Boolean = false,
    val isSimulationMode: Boolean = false
)

class SpeedTracker(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _metrics = MutableStateFlow(LiveMetrics())
    val metrics: StateFlow<LiveMetrics> = _metrics.asStateFlow()

    private var lastLocation: Location? = null
    private var totalDistanceMeters: Float = 0f
    private var maxSpeedKmh: Float = 0f
    private var speedReadingsCount: Int = 0
    private var totalSpeedSumKmh: Float = 0f
    private var isTracking = false

    private var simulationMode = false
    private var simulatedCurrentSpeedKmh = 0f

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!isTracking || simulationMode) return
            val location = result.lastLocation ?: return

            // Accuracy check to filter out huge drifts
            if (location.hasAccuracy() && location.accuracy > 35f) {
                return
            }

            var speedKmh = 0f
            if (location.hasSpeed()) {
                speedKmh = location.speed * 3.6f
            } else if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location)
                val timeDiffSec = (location.time - lastLocation!!.time) / 1000f
                if (timeDiffSec > 0.5f) {
                    speedKmh = (distance / timeDiffSec) * 3.6f
                }
            }

            // Cap extreme GPS anomalies
            if (speedKmh > 40f) speedKmh = 40f
            if (speedKmh < 0.3f) speedKmh = 0f

            if (lastLocation != null) {
                val dist = lastLocation!!.distanceTo(location)
                // Filter out micro-jitter when stationary
                if (dist > 1.2f && (location.accuracy <= 25f || dist > location.accuracy)) {
                    totalDistanceMeters += dist
                }
            }
            lastLocation = location

            updateRecordedSpeed(speedKmh, hasGpsFix = true)
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        simulationMode = enabled
        _metrics.value = _metrics.value.copy(isSimulationMode = enabled)
    }

    fun isSimulationMode(): Boolean = simulationMode

    @SuppressLint("MissingPermission")
    fun startTracking() {
        isTracking = true
        resetMetrics()
        _metrics.value = _metrics.value.copy(isGpsActive = true, isSimulationMode = simulationMode)

        if (!simulationMode) {
            try {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    1500L
                ).setMinUpdateIntervalMillis(1000L)
                    .setMinUpdateDistanceMeters(0.5f)
                    .build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                Log.e("SpeedTracker", "Location request failed: ${e.message}")
            }
        }
    }

    fun stopTracking() {
        isTracking = false
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            Log.e("SpeedTracker", "Failed to remove location updates", e)
        }
        _metrics.value = _metrics.value.copy(
            isGpsActive = false,
            currentSpeedKmh = 0f
        )
    }

    fun pauseTracking() {
        _metrics.value = _metrics.value.copy(currentSpeedKmh = 0f)
    }

    /**
     * Called every second by the timer loop.
     * In simulation mode (for indoor treadmill / emulator test), generates realistic interval speed!
     */
    fun tickSecond(currentPhase: PhaseType) {
        if (!isTracking) return
        if (simulationMode) {
            val targetSpeed = when (currentPhase) {
                PhaseType.FAST -> 11.5f + Random.nextFloat() * 1.8f // Fast interval: 11.5 - 13.3 km/h
                PhaseType.SLOW -> 5.2f + Random.nextFloat() * 1.0f  // Slow interval: 5.2 - 6.2 km/h
            }
            // Smooth acceleration / deceleration toward target
            simulatedCurrentSpeedKmh += (targetSpeed - simulatedCurrentSpeedKmh) * 0.35f
            val distanceIncrement = (simulatedCurrentSpeedKmh / 3.6f) * 1.0f // meters in 1 second
            totalDistanceMeters += distanceIncrement

            updateRecordedSpeed(simulatedCurrentSpeedKmh, hasGpsFix = false)
        }
    }

    private fun updateRecordedSpeed(speedKmh: Float, hasGpsFix: Boolean) {
        if (speedKmh > 0.5f) {
            speedReadingsCount++
            totalSpeedSumKmh += speedKmh
            maxSpeedKmh = max(maxSpeedKmh, speedKmh)
        }

        val avgSpeedKmh = if (speedReadingsCount > 0) totalSpeedSumKmh / speedReadingsCount else 0f
        val paceSeconds = if (speedKmh > 0.8f) {
            (3600f / speedKmh).toInt()
        } else 0

        _metrics.value = LiveMetrics(
            currentSpeedKmh = speedKmh,
            avgSpeedKmh = avgSpeedKmh,
            maxSpeedKmh = maxSpeedKmh,
            totalDistanceMeters = totalDistanceMeters,
            paceSecondsPerKm = paceSeconds,
            isGpsActive = isTracking,
            hasGpsFix = hasGpsFix,
            isSimulationMode = simulationMode
        )
    }

    fun resetMetrics() {
        totalDistanceMeters = 0f
        maxSpeedKmh = 0f
        speedReadingsCount = 0
        totalSpeedSumKmh = 0f
        lastLocation = null
        simulatedCurrentSpeedKmh = 0f
        _metrics.value = LiveMetrics(isSimulationMode = simulationMode)
    }
}
