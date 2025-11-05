package com.example.w05_buble_game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.w05_buble_game.ui.theme.ADPTheme
import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    NewBubbleGameScreen()
                }
            }
        }
    }
}

enum class BubbleType(val color: Color, val score: Int, val timeBonus: Int) {
    NORMAL(Color.Cyan, 10, 0),
    PENALTY(Color.Red, -5, 0),
    TIME_BONUS(Color(0xFF4CAF50), 0, 2)
}

// ## 변경점 1: 버블이 언제 생성됐는지 기록하기 위한 'createdAt' 추가 ##
data class BubbleData(
    val id: String = UUID.randomUUID().toString(),
    val x: Dp,
    val y: Dp,
    val type: BubbleType,
    val size: Dp = 60.dp,
    val createdAt: Long = System.currentTimeMillis() // 생성된 시간 기록
)

@Composable
fun NewBubbleGameScreen() {
    var timeLeft by remember { mutableStateOf(60) }
    var score by remember { mutableStateOf(0) }
    var isGameRunning by remember { mutableStateOf(false) }
    var bubbles by remember { mutableStateOf<List<BubbleData>>(emptyList()) }

    // 게임 타이머 로직
    LaunchedEffect(isGameRunning) {
        if (isGameRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isGameRunning = false
            bubbles = emptyList() // 시간이 다 되면 화면에 남은 버블 모두 제거
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameStatusHeader(score, timeLeft)

        BoxWithConstraints(modifier = Modifier.weight(1f).background(Color.Black)) {
            val maxWidth = maxWidth
            val maxHeight = maxHeight

            // ## 변경점 2: 새로운 '메인 게임 루프' 로직 ##
            // isGameRunning 상태가 true인 동안 이 루프는 계속 반복 실행됩니다.
            LaunchedEffect(isGameRunning) {
                while (isGameRunning) {
                    // 점수에 따라 딜레이 계산 (더 빠르고 역동적으로)
                    val spawnDelay = (400 - score).coerceAtLeast(100).toLong()
                    delay(spawnDelay)

                    // 현재 시간을 기준으로 2.5초가 지난 버블은 제거
                    val now = System.currentTimeMillis()
                    val bubbleLifetime = 2500L
                    val updatedBubbles = bubbles.filter { now - it.createdAt <= bubbleLifetime }.toMutableList()

                    // 버블이 5개 미만이면 새로 추가
                    if (updatedBubbles.size < 5) {
                        updatedBubbles.add(createRandomBubble(maxWidth, maxHeight))
                    }

                    // 최종적으로 화면에 보여줄 버블 리스트 업데이트
                    bubbles = updatedBubbles
                }
            }

            if (isGameRunning) {
                bubbles.forEach { bubbleData ->
                    key(bubbleData.id) {
                        Bubble(
                            data = bubbleData,
                            onClick = {
                                score += bubbleData.type.score
                                timeLeft += bubbleData.type.timeBonus
                                // 클릭된 버블은 즉시 제거
                                bubbles = bubbles.filter { it.id != bubbleData.id }
                            }
                        )
                    }
                }
            } else {
                GameStartOrOverScreen(
                    isGameOver = timeLeft == 0,
                    score = score,
                    onStartClick = {
                        score = 0
                        timeLeft = 60
                        bubbles = emptyList()
                        isGameRunning = true
                    }
                )
            }
        }
    }
}

private fun createRandomBubble(maxWidth: Dp, maxHeight: Dp): BubbleData {
    val bubbleSize = 60.dp
    val randomX = Random.nextInt(0, (maxWidth - bubbleSize).value.toInt()).dp
    val randomY = Random.nextInt(0, (maxHeight - bubbleSize).value.toInt()).dp

    val randomType = when (Random.nextInt(0, 10)) {
        in 0..6 -> BubbleType.NORMAL
        in 7..8 -> BubbleType.PENALTY
        else -> BubbleType.TIME_BONUS
    }
    return BubbleData(x = randomX, y = randomY, type = randomType, size = bubbleSize)
}

@Composable
fun GameStatusHeader(score: Int, timeLeft: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Score: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "Time: $timeLeft", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun BoxScope.GameStartOrOverScreen(isGameOver: Boolean, score: Int, onStartClick: () -> Unit) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isGameOver) {
            Text("Game Over", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Final Score: $score", fontSize = 28.sp, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
        }
        Button(
            onClick = onStartClick,
            modifier = Modifier.padding(16.dp).height(50.dp).width(200.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(if (isGameOver) "Play Again" else "Start Game", fontSize = 18.sp)
        }
    }
}

@Composable
fun Bubble(data: BubbleData, onClick: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(300))
    }
    Box(
        modifier = Modifier
            .offset(x = data.x, y = data.y)
            .size(data.size)
            .scale(scale.value)
            .clip(CircleShape)
            .background(data.type.color)
            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
fun NewBubbleGameScreenPreview() {
    ADPTheme {
        Surface(color = Color.Black) {
            NewBubbleGameScreen()
        }
    }
}