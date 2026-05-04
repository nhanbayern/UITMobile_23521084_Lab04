package com.example.lab04combined

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {
    @Test
    fun formatDuration_formatsMinutesAndSeconds() {
        val result = DurationFormatter.formatDuration(125000)
        assertEquals("02:05", result)
    }
}

