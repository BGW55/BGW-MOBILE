package com.example.w04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.w04.ui.theme.ADPTheme

class W04 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                KeypadScreen()
            }
        }
    }
}

@Composable
fun KeypadScreen() {
    Text("Keypad Screen")
}
