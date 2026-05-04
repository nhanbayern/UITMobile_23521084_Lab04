package com.example.lab04combined.health

import android.content.Context
import android.os.SystemClock
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ExerciseLapSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
// Use Guava ListenableFuture interop await provided by kotlinx-coroutines-guava
import kotlinx.coroutines.guava.await
import java.util.concurrent.Executors

class ExerciseManager(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    // Do not eagerly initialize HealthServices client to avoid binding to the system
    // service on startup. Use lazy initialization only when not in mock mode.
    private val healthClient by lazy { HealthServices.getClient(context) }
    private val exerciseClient by lazy { healthClient.exerciseClient }
    private val executor = Executors.newSingleThreadExecutor()

    // Force mock mode by default so opening the Health UI shows mock data
    // instead of attempting to bind to the Health Services implementation which
    // may not be available in this environment/emulator.
    private var isMockMode = true
    private var sessionStartElapsed = 0L
    private var pauseStartedAt = 0L
    private var accumulatedPauseMillis = 0L

    private var timerJob: Job? = null
    private var mockJob: Job? = null

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    // Minimal ExerciseUpdateCallback implementation using the public API surface
    private val updateCallback = object : ExerciseUpdateCallback {
        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) {
            // no-op for now
        }

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            // Keep minimal: when updates arrive we won't parse metrics yet.
            // If you want the UI to react, set a simple message.
            _uiState.value = _uiState.value.copy(message = "Exercise update received")
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
            // no-op
        }

        override fun onRegistered() {
            // no-op
        }

        override fun onRegistrationFailed(throwable: Throwable) {
            _uiState.value = _uiState.value.copy(message = "Callback registration failed: ${throwable.message}")
        }
    }

    suspend fun checkCapabilities(): Boolean {
        return try {
            val capabilities = exerciseClient.getCapabilitiesAsync().await()
            val supported = capabilities.supportedExerciseTypes.contains(ExerciseType.WALKING)
            isMockMode = !supported
            if (!supported) {
                _uiState.value = _uiState.value.copy(message = "Exercise not available on this device")
            }
            supported
        } catch (ex: Exception) {
            isMockMode = true
            _uiState.value = _uiState.value.copy(message = "Exercise not available on this device")
            false
        }
    }

    fun registerForUpdates() {
        if (!isMockMode) {
            exerciseClient.setUpdateCallback(executor, updateCallback)
        }
    }

    fun unregisterForUpdates() {
        if (!isMockMode) {
            // clear the specific callback that was registered
            exerciseClient.clearUpdateCallbackAsync(updateCallback)
        }
    }

    fun startOrResume() {
        if (_uiState.value.isActive && _uiState.value.isPaused) {
            resumeExercise()
        } else {
            startExercise()
        }
    }

    private fun startExercise() {
        sessionStartElapsed = SystemClock.elapsedRealtime()
        pauseStartedAt = 0L
        accumulatedPauseMillis = 0L
        _uiState.value = _uiState.value.copy(isActive = true, isPaused = false, message = null)

        if (isMockMode) {
            startMockUpdates()
        } else {
            scope.launch {
                try {
                    // Build ExerciseConfig using the builder API
                    val config = ExerciseConfig.builder(ExerciseType.WALKING)
                        .setDataTypes(
                            setOf(
                                DataType.HEART_RATE_BPM,
                                DataType.CALORIES_TOTAL,
                                DataType.DISTANCE_TOTAL,
                                DataType.STEPS_TOTAL
                            )
                        )
                        .build()
                    exerciseClient.startExerciseAsync(config).await()
                } catch (ex: Exception) {
                    isMockMode = true
                    _uiState.value = _uiState.value.copy(message = "Falling back to mock data")
                    startMockUpdates()
                }
            }
        }
        startTimer()
    }

    fun pauseExercise() {
        if (!_uiState.value.isActive || _uiState.value.isPaused) return
        pauseStartedAt = SystemClock.elapsedRealtime()
        _uiState.value = _uiState.value.copy(isPaused = true)
        if (!isMockMode) {
            scope.launch { exerciseClient.pauseExerciseAsync().await() }
        }
    }

    private fun resumeExercise() {
        if (!_uiState.value.isActive || !_uiState.value.isPaused) return
        val now = SystemClock.elapsedRealtime()
        accumulatedPauseMillis += (now - pauseStartedAt)
        pauseStartedAt = 0L
        _uiState.value = _uiState.value.copy(isPaused = false)
        if (!isMockMode) {
            scope.launch { exerciseClient.resumeExerciseAsync().await() }
        }
    }

    fun endExercise() {
        if (!_uiState.value.isActive) return
        if (!isMockMode) {
            scope.launch { exerciseClient.endExerciseAsync().await() }
        }
        stopMockUpdates()
        stopTimer()
        _uiState.value = _uiState.value.copy(isActive = false, isPaused = false, message = "Session ended")
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                val now = SystemClock.elapsedRealtime()
                val duration = if (_uiState.value.isPaused) {
                    pauseStartedAt - sessionStartElapsed - accumulatedPauseMillis
                } else {
                    now - sessionStartElapsed - accumulatedPauseMillis
                }
                _uiState.value = _uiState.value.copy(durationMillis = duration.coerceAtLeast(0L))
                delay(1000L)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startMockUpdates() {
        stopMockUpdates()
        mockJob = scope.launch {
            var heart = 90.0
            var calories = 0.0
            var distance = 0.0
            var steps = 0L
            while (true) {
                if (!_uiState.value.isPaused) {
                    heart = (heart + 1).coerceAtMost(150.0)
                    calories += 0.5
                    distance += 2.5
                    steps += 2
                    _uiState.value = _uiState.value.copy(
                        heartRate = heart,
                        calories = calories,
                        distanceMeters = distance,
                        steps = steps
                    )
                }
                delay(1000L)
            }
        }
    }

    private fun stopMockUpdates() {
        mockJob?.cancel()
        mockJob = null
    }

    // Metric extraction helpers removed temporarily to prioritize compiling.
}

