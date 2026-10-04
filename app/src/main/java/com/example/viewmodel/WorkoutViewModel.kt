package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WorkoutEntity
import com.example.data.WorkoutRepository
import com.example.model.IntervalSegment
import com.example.model.PhaseType
import com.example.model.SpeedUnit
import com.example.model.WorkoutState
import com.example.model.WorkoutSummaryData
import com.example.service.SpeedTracker
import com.example.service.VoiceCoach
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IntervalPattern(val title: String, val subtitle: String) {
    STANDARD_ALTERNATING("1-Min Fast, 1-Min Slow", "Classic morning interval fat-burn (10 intervals)"),
    FAST_FAST_FAST_SLOW("Fast, Fast, Fast, Slow", "High-intensity endurance burn (3 fast, 1 slow)")
}

data class WorkoutUiState(
    val state: WorkoutState = WorkoutState.IDLE,
    val currentIntervalIndex: Int = 1, // 1-based (1 to 10)
    val totalIntervals: Int = 10,
    val currentPhase: PhaseType = PhaseType.FAST,
    val nextPhase: PhaseType = PhaseType.SLOW,
    val intervalDurationSeconds: Int = 60,
    val intervalRemainingSeconds: Int = 60,
    val totalElapsedSeconds: Int = 0,
    val totalWorkoutDurationSeconds: Int = 600, // 10 minutes default
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val voiceCoachEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,

    // Weight Loss & Target Speed parameters
    val userHeightCm: Float = 175f,
    val userWeightKg: Float = 78f,
    val targetWeightKg: Float = 68f,
    val weightLossPlan: com.example.model.WeightLossPlan = com.example.model.WeightLossPlan.calculate(175f, 78f, 68f),
    val showWeightLossDialog: Boolean = false,
    val requiredFastSpeedKmh: Float = 10.5f, // Required speed to run fast for weight loss
    val requiredSlowSpeedKmh: Float = 5.2f,  // Required speed to run slow for recovery
    val continuousCoachingIntervalSec: Int = 15, // Evaluates live speed every 15s continuously
    val continuousCoachingEnabled: Boolean = true,
    val intervalPattern: IntervalPattern = IntervalPattern.STANDARD_ALTERNATING,

    val isSimulationMode: Boolean = false,
    val showSummaryDialog: Boolean = false,
    val lastCompletedSummary: WorkoutSummaryData? = null
) {
    val currentRequiredSpeedKmh: Float
        get() = if (currentPhase == PhaseType.FAST) requiredFastSpeedKmh else requiredSlowSpeedKmh

    val nextRequiredSpeedKmh: Float
        get() = if (nextPhase == PhaseType.FAST) requiredFastSpeedKmh else requiredSlowSpeedKmh
}

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoutRepository
    val speedTracker: SpeedTracker = SpeedTracker(application)
    val voiceCoach: VoiceCoach = VoiceCoach(application)

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    val liveMetrics = speedTracker.metrics

    val pastWorkouts: StateFlow<List<WorkoutEntity>>
    val totalWorkoutsCount: StateFlow<Int>
    val totalDistanceMeters: StateFlow<Float>
    val maxSpeedRecord: StateFlow<Float>

    private var timerJob: Job? = null
    private var secondsSinceLastCoachSpeech = 0

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WorkoutRepository(database.workoutDao())

        pastWorkouts = repository.allWorkouts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        totalWorkoutsCount = repository.totalWorkouts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
        totalDistanceMeters = repository.totalDistanceMeters.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0f
        )
        maxSpeedRecord = repository.maxSpeedRecord.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0f
        )
    }

    private fun getPhaseForInterval(index: Int, pattern: IntervalPattern): PhaseType {
        return when (pattern) {
            IntervalPattern.STANDARD_ALTERNATING -> {
                // 1 Fast, 2 Slow, 3 Fast, 4 Slow...
                if (index % 2 != 0) PhaseType.FAST else PhaseType.SLOW
            }
            IntervalPattern.FAST_FAST_FAST_SLOW -> {
                // 1 Fast, 2 Fast, 3 Fast, 4 Slow, 5 Fast, 6 Fast, 7 Fast, 8 Slow...
                if (index % 4 == 0) PhaseType.SLOW else PhaseType.FAST
            }
        }
    }

    fun startWorkout() {
        if (_uiState.value.state == WorkoutState.RUNNING) return

        speedTracker.startTracking()
        val totalSecs = _uiState.value.totalWorkoutDurationSeconds
        val totalIntervals = (totalSecs / 60).coerceAtLeast(2)
        val pattern = _uiState.value.intervalPattern

        val firstPhase = getPhaseForInterval(1, pattern)
        val secondPhase = getPhaseForInterval(2, pattern)

        _uiState.value = _uiState.value.copy(
            state = WorkoutState.RUNNING,
            currentIntervalIndex = 1,
            totalIntervals = totalIntervals,
            currentPhase = firstPhase,
            nextPhase = secondPhase,
            intervalDurationSeconds = 60,
            intervalRemainingSeconds = 60,
            totalElapsedSeconds = 0,
            showSummaryDialog = false
        )

        secondsSinceLastCoachSpeech = 0
        voiceCoach.announceWorkoutStart(
            totalMinutes = totalSecs / 60,
            requiredFastSpeedKmh = _uiState.value.requiredFastSpeedKmh,
            unit = _uiState.value.speedUnit
        )
        startTimerLoop()
    }

    fun pauseWorkout() {
        if (_uiState.value.state != WorkoutState.RUNNING) return
        timerJob?.cancel()
        speedTracker.pauseTracking()
        _uiState.value = _uiState.value.copy(state = WorkoutState.PAUSED)
        voiceCoach.speak("Workout paused")
    }

    fun resumeWorkout() {
        if (_uiState.value.state != WorkoutState.PAUSED) return
        _uiState.value = _uiState.value.copy(state = WorkoutState.RUNNING)
        val required = _uiState.value.currentRequiredSpeedKmh
        val phaseName = if (_uiState.value.currentPhase == PhaseType.FAST) "Run fast" else "Run slow"
        voiceCoach.speak("Workout resumed! $phaseName. Target speed is ${String.format(java.util.Locale.US, "%.1f", required)} ${_uiState.value.speedUnit.label}")
        startTimerLoop()
    }

    fun skipToNextInterval() {
        if (_uiState.value.state != WorkoutState.RUNNING) return
        advanceToNextInterval()
    }

    fun finishWorkout(earlyFinish: Boolean = false) {
        timerJob?.cancel()
        speedTracker.stopTracking()

        val currentMetrics = speedTracker.metrics.value
        val elapsed = _uiState.value.totalElapsedSeconds
        val completedIntervals = if (earlyFinish) _uiState.value.currentIntervalIndex else _uiState.value.totalIntervals
        val calories = calculateCalories(elapsed, currentMetrics.avgSpeedKmh, _uiState.value.userWeightKg)

        val summary = WorkoutSummaryData(
            totalDurationSeconds = elapsed,
            distanceMeters = currentMetrics.totalDistanceMeters,
            avgSpeedKmh = currentMetrics.avgSpeedKmh,
            maxSpeedKmh = currentMetrics.maxSpeedKmh,
            completedIntervals = completedIntervals,
            totalIntervals = _uiState.value.totalIntervals,
            calories = calories
        )

        _uiState.value = _uiState.value.copy(
            state = WorkoutState.COMPLETED,
            showSummaryDialog = true,
            lastCompletedSummary = summary
        )

        voiceCoach.announceWorkoutComplete(
            distanceKm = currentMetrics.totalDistanceMeters / 1000f,
            avgSpeedKmh = currentMetrics.avgSpeedKmh,
            calories = calories,
            unit = _uiState.value.speedUnit
        )

        // Save to Room DB
        viewModelScope.launch {
            repository.saveWorkout(
                WorkoutEntity(
                    totalDurationSeconds = elapsed,
                    distanceMeters = currentMetrics.totalDistanceMeters,
                    avgSpeedKmh = currentMetrics.avgSpeedKmh,
                    maxSpeedKmh = currentMetrics.maxSpeedKmh,
                    completedIntervals = completedIntervals,
                    totalIntervals = _uiState.value.totalIntervals,
                    calories = calories,
                    workoutTitle = "Morning 10-Min Run"
                )
            )
        }
    }

    fun dismissSummaryDialog() {
        _uiState.value = _uiState.value.copy(
            showSummaryDialog = false,
            state = WorkoutState.IDLE,
            intervalRemainingSeconds = 60,
            currentIntervalIndex = 1,
            currentPhase = PhaseType.FAST,
            nextPhase = PhaseType.SLOW,
            totalElapsedSeconds = 0
        )
        speedTracker.resetMetrics()
    }

    /**
     * "Check Speed" action: immediately tells the runner their live speed and required speed!
     */
    fun checkSpeedNow() {
        val currentSpeed = speedTracker.metrics.value.currentSpeedKmh
        val requiredSpeed = _uiState.value.currentRequiredSpeedKmh
        val isFast = _uiState.value.currentPhase == PhaseType.FAST
        voiceCoach.announceLiveSpeedAssessment(
            currentSpeedKmh = currentSpeed,
            requiredSpeedKmh = requiredSpeed,
            isFastPhase = isFast,
            unit = _uiState.value.speedUnit
        )
    }

    fun toggleVoiceCoach() {
        val newState = !_uiState.value.voiceCoachEnabled
        voiceCoach.isVoiceEnabled = newState
        _uiState.value = _uiState.value.copy(voiceCoachEnabled = newState)
        if (newState) {
            voiceCoach.speak("Voice coach enabled")
        }
    }

    fun toggleSpeedUnit() {
        val nextUnit = if (_uiState.value.speedUnit == SpeedUnit.KMH) SpeedUnit.MPH else SpeedUnit.KMH
        _uiState.value = _uiState.value.copy(speedUnit = nextUnit)
    }

    fun toggleSimulationMode() {
        val nextMode = !_uiState.value.isSimulationMode
        speedTracker.setSimulationMode(nextMode)
        _uiState.value = _uiState.value.copy(isSimulationMode = nextMode)
        val msg = if (nextMode) "Indoor treadmill simulation mode enabled" else "Outdoor GPS mode enabled"
        voiceCoach.speak(msg)
    }

    fun setWorkoutDurationMinutes(minutes: Int) {
        if (_uiState.value.state != WorkoutState.IDLE) return
        val totalSecs = minutes * 60
        val intervals = (totalSecs / 60).coerceAtLeast(2)
        _uiState.value = _uiState.value.copy(
            totalWorkoutDurationSeconds = totalSecs,
            totalIntervals = intervals
        )
    }

    fun setIntervalPattern(pattern: IntervalPattern) {
        if (_uiState.value.state != WorkoutState.IDLE) return
        _uiState.value = _uiState.value.copy(
            intervalPattern = pattern,
            currentPhase = getPhaseForInterval(1, pattern),
            nextPhase = getPhaseForInterval(2, pattern)
        )
    }

    fun showWeightLossDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showWeightLossDialog = show)
    }

    fun applyWeightLossTargetPlan(heightCm: Float, currentWeightKg: Float, targetWeightKg: Float) {
        val plan = com.example.model.WeightLossPlan.calculate(heightCm, currentWeightKg, targetWeightKg)
        _uiState.value = _uiState.value.copy(
            userHeightCm = heightCm,
            userWeightKg = currentWeightKg,
            targetWeightKg = targetWeightKg,
            weightLossPlan = plan,
            requiredFastSpeedKmh = plan.recommendedFastSpeedKmh,
            requiredSlowSpeedKmh = plan.recommendedSlowSpeedKmh,
            showWeightLossDialog = false
        )

        val fastFormatted = String.format(java.util.Locale.US, "%.1f", plan.recommendedFastSpeedKmh)
        val slowFormatted = String.format(java.util.Locale.US, "%.1f", plan.recommendedSlowSpeedKmh)
        val unitLabel = _uiState.value.speedUnit.label

        val message = if (plan.weightToLoseKg > 0.5f) {
            "Personalized weight loss plan set! To lose ${String.format(java.util.Locale.US, "%.1f", plan.weightToLoseKg)} kilograms, your fast sprint speed is set to $fastFormatted $unitLabel, and slow recovery speed is $slowFormatted $unitLabel for your daily 10-minute run."
        } else {
            "Weight target set! Fast speed is $fastFormatted $unitLabel, and slow recovery is $slowFormatted $unitLabel."
        }
        voiceCoach.speak(message)
    }

    fun setRequiredSpeeds(fastSpeedKmh: Float, slowSpeedKmh: Float) {
        _uiState.value = _uiState.value.copy(
            requiredFastSpeedKmh = fastSpeedKmh,
            requiredSlowSpeedKmh = slowSpeedKmh
        )
    }

    fun setContinuousCoachingInterval(seconds: Int) {
        _uiState.value = _uiState.value.copy(continuousCoachingIntervalSec = seconds)
    }

    fun setContinuousCoachingEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(continuousCoachingEnabled = enabled)
    }

    fun setUserWeightKg(weight: Float) {
        _uiState.value = _uiState.value.copy(userWeightKg = weight)
    }

    fun deleteWorkoutRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.state == WorkoutState.RUNNING) {
                delay(1000L)
                tickOneSecond()
            }
        }
    }

    private fun tickOneSecond() {
        val current = _uiState.value
        val remainingInInterval = current.intervalRemainingSeconds - 1
        val newElapsed = current.totalElapsedSeconds + 1

        speedTracker.tickSecond(current.currentPhase)
        secondsSinceLastCoachSpeech++

        // Audio countdown for final 3 seconds before 1-minute switch
        if (remainingInInterval in 1..3) {
            voiceCoach.announceCountdown(remainingInInterval)
        }

        // Continuous live speed coaching evaluation!
        // Tells runner: "You are running slow! Your speed is X, required speed is Y, run fast!"
        if (current.continuousCoachingEnabled &&
            remainingInInterval > 5 &&
            secondsSinceLastCoachSpeech >= current.continuousCoachingIntervalSec
        ) {
            val liveSpeed = speedTracker.metrics.value.currentSpeedKmh
            voiceCoach.announceLiveSpeedAssessment(
                currentSpeedKmh = liveSpeed,
                requiredSpeedKmh = current.currentRequiredSpeedKmh,
                isFastPhase = current.currentPhase == PhaseType.FAST,
                unit = current.speedUnit
            )
            secondsSinceLastCoachSpeech = 0
        }

        if (remainingInInterval <= 0) {
            // Interval completed!
            if (current.currentIntervalIndex >= current.totalIntervals) {
                // Entire workout completed!
                finishWorkout(earlyFinish = false)
            } else {
                advanceToNextInterval()
            }
        } else {
            _uiState.value = current.copy(
                intervalRemainingSeconds = remainingInInterval,
                totalElapsedSeconds = newElapsed
            )
        }
    }

    private fun advanceToNextInterval() {
        val current = _uiState.value
        val nextIntervalIndex = current.currentIntervalIndex + 1
        val newCurrentPhase = getPhaseForInterval(nextIntervalIndex, current.intervalPattern)
        val upcomingPhase = if (nextIntervalIndex < current.totalIntervals) {
            getPhaseForInterval(nextIntervalIndex + 1, current.intervalPattern)
        } else {
            PhaseType.SLOW
        }

        val requiredSpeed = if (newCurrentPhase == PhaseType.FAST) current.requiredFastSpeedKmh else current.requiredSlowSpeedKmh

        _uiState.value = current.copy(
            currentIntervalIndex = nextIntervalIndex,
            currentPhase = newCurrentPhase,
            nextPhase = upcomingPhase,
            intervalRemainingSeconds = 60
        )

        secondsSinceLastCoachSpeech = 0

        // Announce new phase and required speed immediately after 1 minute switch!
        voiceCoach.announcePhaseChange(
            phase = newCurrentPhase,
            intervalNumber = nextIntervalIndex,
            totalIntervals = current.totalIntervals,
            requiredSpeedKmh = requiredSpeed,
            unit = current.speedUnit
        )
    }

    private fun calculateCalories(durationSec: Int, avgSpeedKmh: Float, weightKg: Float): Int {
        val met = if (avgSpeedKmh > 10.5f) 11.5f else if (avgSpeedKmh > 8f) 9.0f else if (avgSpeedKmh > 5f) 6.5f else 3.8f
        val minutes = durationSec / 60f
        return (0.0175f * met * weightKg * minutes).toInt().coerceAtLeast(1)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        speedTracker.stopTracking()
        voiceCoach.shutdown()
    }
}
