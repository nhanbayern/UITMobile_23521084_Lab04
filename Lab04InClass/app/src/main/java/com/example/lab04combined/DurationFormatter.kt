package com.example.lab04combined

import java.util.concurrent.TimeUnit

object DurationFormatter {
    fun formatDuration(elapsedMillis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

