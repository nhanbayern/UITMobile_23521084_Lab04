package com.example.lab04combined.health

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lab04combined.DurationFormatter
import kotlinx.coroutines.launch

class HealthActivity : ComponentActivity() {
    private lateinit var exerciseManager: ExerciseManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            // Permission is required for real sensors; mock data will still be shown if enabled.
            exerciseManager.registerForUpdates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseManager = ExerciseManager(this)
        requestPermissionsIfNeeded()

        setContent {
            val uiState = exerciseManager.uiState.collectAsStateWithLifecycle().value
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = DurationFormatter.formatDuration(uiState.durationMillis),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricItem(label = "HR", value = uiState.heartRate?.toInt()?.toString() ?: "--")
                            Spacer(modifier = Modifier.width(24.dp))
                            MetricItem(label = "CAL", value = uiState.calories?.toInt()?.toString() ?: "--")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricItem(label = "KM", value = uiState.distanceMeters?.let { String.format("%.2f", it / 1000.0) } ?: "--")
                            Spacer(modifier = Modifier.width(24.dp))
                            MetricItem(label = "STP", value = uiState.steps?.toString() ?: "--")
                        }

                        uiState.message?.let { message ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = message, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(
                            onClick = {
                                lifecycleScope.launch {
                                    exerciseManager.checkCapabilities()
                                    exerciseManager.startOrResume()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF8C00)),
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text(text = if (uiState.isPaused || !uiState.isActive) "START" else "RUN", color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { exerciseManager.pauseExercise() },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text(text = "PAUSE")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { exerciseManager.endExercise() },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text(text = "END")
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        exerciseManager.registerForUpdates()
    }

    override fun onStop() {
        exerciseManager.unregisterForUpdates()
        super.onStop()
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        val missing = permissions.any { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missing) {
            permissionLauncher.launch(permissions)
        }
    }
}

@androidx.compose.runtime.Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Labels doubled in size
        Text(text = label, color = Color.Gray, fontSize = 24.sp)
        // Values doubled in size
        Text(text = value, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}
