package com.example.wop_rhythmgame

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wop_rhythmgame.ui.theme.ADPTheme
import kotlinx.coroutines.isActive
import kotlin.math.abs

// -------------------------------------------------------------------
// 1. MainActivity (앱 진입점)
// -------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADPTheme {
                // RhythmGameScreen이 게임 흐름(Flow)을 관리합니다.
                RhythmGameScreen()
            }
        }
    }
}

// -------------------------------------------------------------------
// 2. 게임 흐름(Flow) 관리
// -------------------------------------------------------------------

// 게임의 전체 상태 (로딩, 플레이 중, 결과)
enum class GameFlow {
    LOADING,
    PLAYING,
    RESULT
}

@Composable
fun RhythmGameScreen() {
    val context = LocalContext.current

    // 현재 게임 흐름 상태
    var gameFlow by remember { mutableStateOf(GameFlow.LOADING) }

    // MediaPlayer 인스턴스 (Nullable)
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }

    // 비트맵 (로딩 완료 후 생성됨)
    var beatmap by remember { mutableStateOf<List<Note>>(emptyList()) }

    // 최종 점수/콤보 (결과 화면용)
    var finalScore by remember { mutableStateOf(0) }
    var maxCombo by remember { mutableStateOf(0) }

    // --- MediaPlayer 준비 로직 ---
    // Composable이 처음 시작될 때 MediaPlayer 준비
    LaunchedEffect(Unit) {
        try {
            // [중요] R.raw.my_song을 본인의 파일명으로 변경하세요.
            val mp = MediaPlayer.create(context, R.raw.seishunsubliminalflac).apply {
                setOnPreparedListener {
                    // 준비가 완료되면 비트맵 생성 및 게임 시작!
                    beatmap = createRandomBeatmap(it.duration.toLong())
                    mediaPlayer.value = it
                    gameFlow = GameFlow.PLAYING // 상태 변경
                }
                // (create는 prepareAsync()가 아닌 prepare()를 내부적으로 호출하므로,
                // setOnPreparedListener 대신 바로 로직을 실행해도 되지만,
                // 안전을 위해 이 구조를 유지하는 것이 좋습니다.
                // 여기서는 create가 완료되면 바로 prepared 상태가 됩니다.)
            }
            // MediaPlayer.create는 준비가 완료된 상태의 객체를 반환하거나, 실패 시 null/예외를 반환함.
            // 따라서 리스너 대신 직접 상태 변경
            if (mp != null) {
                beatmap = createRandomBeatmap(mp.duration.toLong())
                mediaPlayer.value = mp
                gameFlow = GameFlow.PLAYING
            } else {
                // TODO: 로딩 실패 처리
            }

        } catch (e: Exception) {
            // TODO: 파일 로딩 실패 처리 (예: 파일이 없는 경우)
            e.printStackTrace()
        }
    }

    // --- Composable이 사라질 때 MediaPlayer 자원 해제 ---
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.value?.stop()
            mediaPlayer.value?.release()
            mediaPlayer.value = null
        }
    }

    // --- 현재 게임 흐름(Flow)에 따라 다른 화면 표시 ---
    when (gameFlow) {
        GameFlow.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("로딩 중...", fontSize = 32.sp)
            }
        }
        GameFlow.PLAYING -> {
            mediaPlayer.value?.let { mp ->
                GameScreen(
                    mediaPlayer = mp,
                    beatmap = beatmap, // 생성된 랜덤 비트맵 전달
                    onGameEnd = { score, combo ->
                        // 게임이 끝나면 결과 저장 및 상태 변경
                        finalScore = score
                        maxCombo = combo
                        gameFlow = GameFlow.RESULT
                    }
                )
            }
        }
        GameFlow.RESULT -> {
            ResultScreen(
                score = finalScore,
                maxCombo = maxCombo,
                onBackToMenu = {
                    // TODO: "다시하기" 또는 "메뉴로" 로직 필요.
                    // 여기서는 간단히 앱을 재시작해야 함 (또는 로딩부터 다시 시작)
                    gameFlow = GameFlow.LOADING // (간단히 로딩으로 되돌리기)
                    // 앱 재시작을 원하면: (context as? Activity)?.recreate()
                }
            )
        }
    }
}

// -------------------------------------------------------------------
// 3. 게임 화면 (게임, 결과)
// -------------------------------------------------------------------

@Composable
fun ResultScreen(score: Int, maxCombo: Int, onBackToMenu: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("게임 종료!", fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Text("최종 점수: $score", fontSize = 32.sp)
        Text("최대 콤보: $maxCombo", fontSize = 32.sp)
        Spacer(modifier = Modifier.height(64.dp))
        // TODO: "다시하기" 버튼 (onBackToMenu 호출)
        // Button(onClick = onBackToMenu) {
        //     Text("다시하기", fontSize = 24.sp)
        // }
    }
}

/**
 * 실제 게임 플레이가 이루어지는 화면
 */
@Composable
fun GameScreen(
    mediaPlayer: MediaPlayer,
    beatmap: List<Note>,
    onGameEnd: (score: Int, maxCombo: Int) -> Unit
) {
    // --- GameState 생성 (로직과 상태 관리) ---
    val gameState = rememberRhythmGameState(beatmap = beatmap)

    // --- 게임 루프 실행 ---
    LaunchedEffect(gameState, mediaPlayer) {
        mediaPlayer.start() // 여기서 게임(음악) 시작

        while (isActive && mediaPlayer.isPlaying) {
            // 매 프레임마다 GameState를 업데이트
            withFrameNanos {
                gameState.update(mediaPlayer.currentPosition.toLong())
            }
        }

        // 루프가 끝나면 (노래가 끝나면)
        onGameEnd(gameState.score, gameState.maxCombo)
    }

    // --- 렌더러 호출 (UI 그리기 및 입력) ---
    GameRenderer(gameState = gameState)
}


// -------------------------------------------------------------------
// 4. GameRenderer (UI - 그리기 및 입력)
// (이전 코드와 동일 - 변경 없음)
// -------------------------------------------------------------------
@Composable
fun GameRenderer(
    gameState: RhythmGameState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val tapTimeMs = gameState.currentGameTimeMs

                        event.changes.forEach { change ->
                            val laneWidth = size.width / NUM_LANES.toFloat()
                            val tappedLane = (change.position.x / laneWidth).toInt().coerceIn(0, NUM_LANES - 1)

                            when {
                                change.pressed && !change.previousPressed -> {
                                    gameState.onLanePressed(tappedLane, tapTimeMs)
                                }
                                !change.pressed && change.previousPressed -> {
                                    gameState.onLaneReleased(tappedLane, tapTimeMs)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        val hitLineY = size.height * 0.8f
        drawLanes(hitLineY)
        drawNotes(gameState, hitLineY)
        drawOverlay(gameState, textMeasurer, hitLineY)
    }
}

// --- Renderer 헬퍼 함수 (이전 코드와 동일 - 변경 없음) ---

private fun DrawScope.drawLanes(hitLineY: Float) {
    val laneWidth = size.width / NUM_LANES.toFloat()
    for (i in 0 until NUM_LANES) {
        drawRect(
            color = if (i % 2 == 0) Color.DarkGray else Color.Gray,
            topLeft = Offset(i * laneWidth, 0f),
            size = Size(laneWidth, size.height)
        )
    }
    drawLine(
        color = Color.Red,
        start = Offset(0f, hitLineY),
        end = Offset(size.width, hitLineY),
        strokeWidth = 5f
    )
}

private fun DrawScope.drawNotes(
    gameState: RhythmGameState,
    hitLineY: Float
) {
    val laneWidth = size.width / NUM_LANES.toFloat()
    val noteHeadHeight = 50f

    fun getYForTime(timeMs: Long): Float {
        val timeSinceSpawn = gameState.currentGameTimeMs - (timeMs - NOTE_TRAVEL_TIME_MS)
        val travelProgress = timeSinceSpawn.toFloat() / NOTE_TRAVEL_TIME_MS.toFloat()
        return travelProgress * hitLineY
    }

    gameState.notes.forEach { note ->
        val noteX = note.lane * laneWidth
        val yHead = getYForTime(note.hitTimeMs)
        if (note.durationMs > 0L) {
            val yTail = getYForTime(note.hitTimeMs + note.durationMs)
            val tailColor = if (gameState.activeLongNotes[note.lane] == note) Color.Cyan else Color.Blue
            drawRect(
                color = tailColor.copy(alpha = 0.7f),
                topLeft = Offset(noteX, yTail),
                size = Size(laneWidth, yHead - yTail)
            )
        }
        val headColor = if (gameState.activeLongNotes[note.lane] == note) Color.Yellow else Color.Cyan
        drawRect(
            color = headColor,
            topLeft = Offset(noteX, yHead),
            size = Size(laneWidth, noteHeadHeight)
        )
    }
}

private fun DrawScope.drawOverlay(
    gameState: RhythmGameState,
    textMeasurer: TextMeasurer,
    hitLineY: Float
) {
    // 판정 & 콤보 텍스트
    gameState.lastJudgment?.let { judgment ->
        val timeSinceJudgment = gameState.currentGameTimeMs - gameState.lastJudgmentTime
        if (timeSinceJudgment < JUDGMENT_TEXT_DURATION_MS) {
            val alpha = 1f - (timeSinceJudgment.toFloat() / JUDGMENT_TEXT_DURATION_MS)
            val judgmentTextStyle = TextStyle(
                color = judgment.color.copy(alpha = alpha), fontSize = 48.sp, fontWeight = FontWeight.Bold
            )
            val judgmentLayout = textMeasurer.measure(judgment.text, judgmentTextStyle)
            val judgmentY = hitLineY - judgmentLayout.size.height * 2
            drawText(
                textLayoutResult = judgmentLayout,
                topLeft = Offset((size.width - judgmentLayout.size.width) / 2f, judgmentY)
            )
            if (judgment != Judgment.MISS && gameState.combo > 1) {
                val comboTextStyle = TextStyle(
                    color = Color.White.copy(alpha = alpha), fontSize = 40.sp, fontWeight = FontWeight.Normal
                )
                val comboLayout = textMeasurer.measure("${gameState.combo}", comboTextStyle)
                drawText(
                    textLayoutResult = comboLayout,
                    topLeft = Offset((size.width - comboLayout.size.width) / 2f, judgmentY + judgmentLayout.size.height)
                )
            }
        }
    }
    // 점수 텍스트
    val scoreTextStyle = TextStyle(color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    val scoreTextLayoutResult = textMeasurer.measure("Score: ${gameState.score}", scoreTextStyle)
    drawText(
        textLayoutResult = scoreTextLayoutResult,
        topLeft = Offset(size.width - scoreTextLayoutResult.size.width - 40f, 40f)
    )
}

// -------------------------------------------------------------------
// 5. RhythmGameState (로직 - 상태 홀더 클래스)
// -------------------------------------------------------------------
@Composable
fun rememberRhythmGameState(
    beatmap: List<Note>
): RhythmGameState {
    return remember { RhythmGameState(beatmap) }
}

class RhythmGameState(
    private val beatmap: List<Note>
) {
    // --- 1. 게임의 모든 '상태' ---
    var score by mutableStateOf(0)
        private set
    var combo by mutableStateOf(0)
        private set
    var maxCombo by mutableStateOf(0) // 최대 콤보
        private set

    val notes = mutableStateListOf<Note>()
    var currentGameTimeMs by mutableStateOf(0L)
        private set
    var lastJudgment by mutableStateOf<Judgment?>(null)
        private set
    var lastJudgmentTime by mutableStateOf(0L)
        private set
    val activeLongNotes = mutableStateMapOf<Int, Note>()
    val holdingLanes = mutableStateMapOf<Int, Boolean>()

    // --- 2. 게임 '로직' (상태를 변경하는 함수) ---

    fun update(newTimeMs: Long) {
        currentGameTimeMs = newTimeMs
        spawnNotes()
        checkMissesAndBreaks()
    }

    private fun spawnNotes() {
        beatmap.forEach { noteData ->
            val spawnTimeMs = noteData.hitTimeMs - NOTE_TRAVEL_TIME_MS
            if (currentGameTimeMs >= spawnTimeMs && notes.none { it.id == noteData.id }) {
                notes.add(noteData)
            }
        }
    }

    private fun checkMissesAndBreaks() {
        val missedNotes = notes.filter { note ->
            val isMissed = (currentGameTimeMs > note.hitTimeMs + MISS_THRESHOLD_MS)
            isMissed && !activeLongNotes.containsValue(note)
        }
        if (missedNotes.isNotEmpty()) {
            missedNotes.forEach {
                performJudgment(Judgment.MISS, currentGameTimeMs)
                notes.remove(it)
            }
        }
        activeLongNotes.forEach { (lane, note) ->
            if (holdingLanes[lane] == false) {
                performJudgment(Judgment.MISS, currentGameTimeMs)
                activeLongNotes.remove(lane)
                notes.remove(note)
            }
        }
    }

    private fun performJudgment(judgment: Judgment, time: Long) {
        lastJudgment = judgment
        lastJudgmentTime = time
        if (judgment == Judgment.MISS) {
            combo = 0
        } else {
            combo++
            if (combo > maxCombo) { // 최대 콤보 갱신
                maxCombo = combo
            }
            score += if (judgment == Judgment.PERFECT) 100 else 50
        }
    }

    fun onLanePressed(lane: Int, tapTimeMs: Long) {
        holdingLanes[lane] = true
        val targetNote = notes
            .filter { it.lane == lane && !activeLongNotes.containsValue(it) }
            .minByOrNull { abs(it.hitTimeMs - tapTimeMs) }

        if (targetNote != null) {
            val timeDiff = abs(targetNote.hitTimeMs - tapTimeMs)
            val judgment = when {
                timeDiff <= PERFECT_WINDOW_MS -> Judgment.PERFECT
                timeDiff <= GOOD_WINDOW_MS -> Judgment.GOOD
                else -> null
            }
            if (judgment != null) {
                performJudgment(judgment, tapTimeMs)
                if (targetNote.durationMs > 0L) {
                    activeLongNotes[lane] = targetNote
                } else {
                    notes.remove(targetNote)
                }
            }
        }
    }

    fun onLaneReleased(lane: Int, releaseTimeMs: Long) {
        holdingLanes[lane] = false
        val heldNote = activeLongNotes[lane]

        if (heldNote != null) {
            val endTimeMs = heldNote.hitTimeMs + heldNote.durationMs
            val releaseDiff = abs(endTimeMs - releaseTimeMs)
            val judgment = when {
                releaseDiff <= PERFECT_WINDOW_MS -> Judgment.PERFECT
                releaseDiff <= GOOD_WINDOW_MS -> Judgment.GOOD
                else -> Judgment.MISS
            }
            performJudgment(judgment, releaseTimeMs)
            activeLongNotes.remove(lane)
            notes.remove(heldNote)
        }
    }
}

// -------------------------------------------------------------------
// 6. 데이터 모델 및 상수
// (이전 코드와 동일)
// -------------------------------------------------------------------

data class Note(
    val id: Int,
    val lane: Int,
    val hitTimeMs: Long,
    val durationMs: Long = 0L
)

enum class Judgment(val text: String, val color: Color) {
    PERFECT("PERFECT", Color.Yellow),
    GOOD("GOOD", Color.Green),
    MISS("MISS", Color.Red)
}

const val NUM_LANES = 4
const val NOTE_TRAVEL_TIME_MS = 1000L
const val PERFECT_WINDOW_MS = 50L
const val GOOD_WINDOW_MS = 100L
const val MISS_THRESHOLD_MS = 200L
const val JUDGMENT_TEXT_DURATION_MS = 500L

// -------------------------------------------------------------------
// 7. 랜덤 비트맵 생성기
// -------------------------------------------------------------------

/**
 * 음악 길이에 맞춰 랜덤 비트맵을 생성합니다.
 */
fun createRandomBeatmap(durationMs: Long): List<Note> {
    val notes = mutableListOf<Note>()
    var nextNoteId = 0

    // 2초(2000ms)부터 시작해서 0.5초(500ms) 간격으로 노트 생성 시도
    var currentTimeMs = 2000L
    val intervalMs = 500L // 0.5초 간격

    while (currentTimeMs < durationMs - 2000L) { // 노래 끝나기 2초 전에 멈춤
        val randomLane = (0 until NUM_LANES).random()
        val isLongNote = kotlin.random.Random.nextInt(100) < 20 // 20% 확률 롱노트
        val duration = if (isLongNote) kotlin.random.Random.nextLong(300, 1000) else 0L

        val hitTime = currentTimeMs

        if (hitTime + duration > durationMs - 1000L) {
            currentTimeMs += intervalMs
            continue
        }

        val isOverlapping = notes.any {
            it.lane == randomLane && abs(it.hitTimeMs - hitTime) < 200L
        }

        if (!isOverlapping) {
            notes.add(
                Note(
                    id = nextNoteId++,
                    lane = randomLane,
                    hitTimeMs = hitTime,
                    durationMs = duration
                )
            )
        }

        currentTimeMs += intervalMs
    }
    return notes
}