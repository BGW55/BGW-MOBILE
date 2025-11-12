package com.example.w04

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.w04.ui.theme.ADPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADPTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(), //전체 화면을 차지
            contentAlignment = Alignment.Center // 중앙 정렬
        ) {
            // MessageCard(Message("Android", "ADP Compose"))
            // ProfileCard(Profile("배건우", "안드로이드 앱 개발자"))
        }
    }
}

data class Message(val name: String, val msg: String)
data class Profile(val name: String, val intro: String)
