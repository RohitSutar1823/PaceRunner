package com.example.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.model.PhaseType
import com.example.model.SpeedUnit
import java.util.Locale

class VoiceCoach(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isVoiceEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(1.08f)
            tts?.setPitch(1.0f)
            isTtsReady = true
            Log.d("VoiceCoach", "TTS Initialized successfully")
        } else {
            Log.e("VoiceCoach", "TTS Initialization failed: $status")
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!isVoiceEnabled || !isTtsReady) return
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "VOICE_COACH_${System.currentTimeMillis()}")
    }

    fun announceWorkoutStart(totalMinutes: Int, requiredFastSpeedKmh: Float, unit: SpeedUnit) {
        val reqSpeedFormatted = formatSpeed(requiredFastSpeedKmh, unit)
        val unitLabel = getUnitSpoken(unit)
        speak("Morning Run started! $totalMinutes minutes interval workout for weight loss. Interval 1: Run fast for 1 minute! Your required speed is $reqSpeedFormatted $unitLabel. Let's go!", true)
        vibratePhaseSwitch(isFast = true)
    }

    fun announcePhaseChange(phase: PhaseType, intervalNumber: Int, totalIntervals: Int, requiredSpeedKmh: Float, unit: SpeedUnit) {
        val reqSpeedFormatted = formatSpeed(requiredSpeedKmh, unit)
        val unitLabel = getUnitSpoken(unit)

        val message = when (phase) {
            PhaseType.FAST -> "Run fast! Interval $intervalNumber of $totalIntervals. Sprint now! Your required speed is $reqSpeedFormatted $unitLabel."
            PhaseType.SLOW -> "Run slow! Interval $intervalNumber of $totalIntervals. Slow down and recover! Your required speed is $reqSpeedFormatted $unitLabel."
        }
        speak(message, true)
        vibratePhaseSwitch(isFast = phase == PhaseType.FAST)
    }

    /**
     * Continuous live coaching called periodically (e.g. every 10-15 seconds)
     * Compares live speed with required target speed for weight loss!
     */
    fun announceLiveSpeedAssessment(
        currentSpeedKmh: Float,
        requiredSpeedKmh: Float,
        isFastPhase: Boolean,
        unit: SpeedUnit
    ) {
        if (!isVoiceEnabled) return

        val curSpeedFormatted = formatSpeed(currentSpeedKmh, unit)
        val reqSpeedFormatted = formatSpeed(requiredSpeedKmh, unit)
        val unitLabel = getUnitSpoken(unit)

        val message = if (isFastPhase) {
            if (currentSpeedKmh < 0.6f) {
                "You are stationary! Run fast! Your required speed is $reqSpeedFormatted $unitLabel."
            } else if (currentSpeedKmh < requiredSpeedKmh - 1.0f) {
                "You are running slow! Your speed is $curSpeedFormatted $unitLabel. Run fast! Your required speed is $reqSpeedFormatted $unitLabel for your weight loss target."
            } else {
                "Great job! You are running fast at $curSpeedFormatted $unitLabel. Your required speed is $reqSpeedFormatted $unitLabel. Keep pushing!"
            }
        } else {
            // SLOW recovery phase
            if (currentSpeedKmh > requiredSpeedKmh + 1.8f) {
                "You are running too fast! Your speed is $curSpeedFormatted $unitLabel. Run slow! Your required recovery speed is $reqSpeedFormatted $unitLabel to burn fat."
            } else if (currentSpeedKmh < 0.5f) {
                "Keep jogging slow! Your required recovery speed is $reqSpeedFormatted $unitLabel."
            } else {
                "Good pace! You are running slow at $curSpeedFormatted $unitLabel. Breathe and recover."
            }
        }

        speak(message, flush = true)
    }

    fun announceCountdown(seconds: Int) {
        if (seconds in 1..3) {
            speak("$seconds", flush = false)
        }
    }

    fun announceSpeed(speedKmh: Float, requiredSpeedKmh: Float, isFastPhase: Boolean, unit: SpeedUnit) {
        announceLiveSpeedAssessment(speedKmh, requiredSpeedKmh, isFastPhase, unit)
    }

    fun announceWorkoutComplete(distanceKm: Float, avgSpeedKmh: Float, calories: Int, unit: SpeedUnit) {
        val distFormatted = String.format(Locale.US, "%.2f", if (unit == SpeedUnit.KMH) distanceKm else distanceKm * 0.621371f)
        val speedFormatted = String.format(Locale.US, "%.1f", if (unit == SpeedUnit.KMH) avgSpeedKmh else avgSpeedKmh * 0.621371f)
        val distUnit = if (unit == SpeedUnit.KMH) "kilometers" else "miles"
        val speedUnit = if (unit == SpeedUnit.KMH) "kilometers per hour" else "miles per hour"
        speak("Workout crushed! 10-minute morning interval workout completed! You burned $calories calories and ran $distFormatted $distUnit with an average speed of $speedFormatted $speedUnit. Outstanding work!", true)
    }

    fun vibratePhaseSwitch(isFast: Boolean) {
        if (!isHapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (isFast) {
                    val timings = longArrayOf(0, 180, 80, 260)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(450, 180))
                }
            } else {
                @Suppress("DEPRECATION")
                if (isFast) {
                    vibrator.vibrate(longArrayOf(0, 180, 80, 260), -1)
                } else {
                    vibrator.vibrate(450)
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceCoach", "Error vibrating", e)
        }
    }

    private fun formatSpeed(speedKmh: Float, unit: SpeedUnit): String {
        val converted = if (unit == SpeedUnit.KMH) speedKmh else speedKmh * 0.621371f
        return String.format(Locale.US, "%.1f", converted)
    }

    private fun getUnitSpoken(unit: SpeedUnit): String {
        return if (unit == SpeedUnit.KMH) "kilometers per hour" else "miles per hour"
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }
}
