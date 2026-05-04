package com.example.lab04combined.health

data class ExerciseUiState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val durationMillis: Long = 0L,
    val heartRate: Double? = null,
    val calories: Double? = null,
    val distanceMeters: Double? = null,
    val steps: Long? = null,
    val message: String? = null
)

