package com.example.w03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.w03.ui.theme.ADPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADPTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // 배경색 검정색
            .padding(horizontal = 32.dp, vertical = 48.dp), // 전체적인 여백 추가
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Samsung Foundry",
            style = MaterialTheme.typography.displaySmall, // 더 큰 제목 스타일
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp)) // 텍스트 간 여백
        Text(
            text = "삼성 엑시노스",
            style = MaterialTheme.typography.headlineMedium, // 중간 크기 제목 스타일
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp)) // 이미지 위 여백
        Image(
            painter = painterResource(id = R.drawable.exynos1),
            contentDescription = "엑시노스 로고",
            modifier = Modifier
                .size(280.dp) // 이미지 크기 약간 조정
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp)) // 이미지 아래 여백
        Text(
            text = "엑시노스는 어쩌다 망작이 되었는가",
            style = MaterialTheme.typography.titleMedium, // 본문 또는 부연 설명 스타일
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000) // Preview 배경 검정색으로
@Composable
fun HomeScreenPreview() {
    ADPTheme {
        HomeScreen()
    }
}