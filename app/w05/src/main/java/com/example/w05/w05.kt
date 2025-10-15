package com.example.w05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.w05.ui.theme.ADPTheme
import kotlinx.coroutines.delay

class w05 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StopwatchScreen()
                }
            }
        }
    }
}

@Composable
fun StopwatchScreen() {
    var isRunning by remember { mutableStateOf(false) }
    var timeMillis by remember { mutableStateOf(0L) }
    var laps by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(key1 = isRunning) {
        while (isRunning) {
            delay(10) // Update every 10 milliseconds
            timeMillis += 10
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time display
        Text(
            text = formatTime(timeMillis),
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { isRunning = true }, enabled = !isRunning) {
                Text("Start")
            }
            Button(
                onClick = { isRunning = false },
                enabled = isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Stop")
            }
            Button(
                onClick = { laps = laps + formatTime(timeMillis) },
                enabled = isRunning
            ) {
                Text("Lap")
            }
            Button(onClick = {
                isRunning = false
                timeMillis = 0L
                laps = emptyList()
            }) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Lap times
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(laps) { lapTime ->
                Text(text = lapTime, fontSize = 20.sp)
            }
        }
    }
}

fun formatTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val milliseconds = (timeMillis % 1000) / 10
    return String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
}

@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    ADPTheme {
        StopwatchScreen()
    }
}