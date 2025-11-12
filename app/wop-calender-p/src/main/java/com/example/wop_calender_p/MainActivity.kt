package com.example.wop_calender_p

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.wop_calender_p.ui.theme.ADPTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

// --- 상수 정의 ---
private val HOUR_HEIGHT = 60.dp
private val GridLineColor = Color.LightGray.copy(alpha = 0.5f)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("YYYY년 M월")
private val dayFormatter = DateTimeFormatter.ofPattern("d")
private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val DAYS_OF_WEEK = listOf("일", "월", "화", "수", "목", "금", "토")
private val EVENT_COLORS = listOf(
    Color(0xFFF4511E), Color(0xFF0B8043), Color(0xFF3367D6), Color(0xFF8E24AA),
    Color(0xFFE67C73), Color(0xFFF6BF26), Color(0xFF009688), Color(0xFFD50000)
)

private fun MainActivity.WopcalenderTheme(function: @Composable () -> Unit) {}
// --- 데이터 클래스 ---
data class Event(
    val id: Long,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val color: Color
)

// 화면에 표시될 이벤트의 레이아웃 정보를 담는 클래스
open class EventLayout(
    val event: Event,
    val top: Dp,
    val height: Dp,
    var left: Float, // 0.0f ~ 1.0f (부모 너비 대비 비율)
    var width: Float // 0.0f ~ 1.0f (부모 너비 대비 비율)
)


// --- 헬퍼 함수 ---
private fun dpToLocalDateTime(offsetY: Dp, date: LocalDate): LocalDateTime {
    // 수정: Dp/Dp의 결과는 Float이므로, 불필요한 .value를 제거
    val minutes = (offsetY / HOUR_HEIGHT * 60).toLong()
    return date.atStartOfDay().plusMinutes(minutes)
}

private fun minutesToDp(minutes: Long): Dp {
    return (minutes / 60f).toFloat().dp * HOUR_HEIGHT.value
}

// --- MainActivity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WopcalenderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeeklyCalendarScreen()
                }
            }
        }
    }

    // --- ⬇️ 여기 있던 TODO 함수 삭제됨 ---
}


// --- 메인 화면 Composable ---
@Composable
fun WeeklyCalendarScreen() {
    // 1. 상태 관리
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(LocalDate.now())) }
    var events by remember { mutableStateOf(getSampleEvents()) }

    // 2. UI 구조
    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 헤더 (날짜, 주 이동 버튼)
        CalendarHeader(
            startOfWeek = currentWeekStart,
            onPrevWeek = { currentWeekStart = currentWeekStart.minusWeeks(1) },
            onNextWeek = { currentWeekStart = currentWeekStart.plusWeeks(1) },
            onToday = { currentWeekStart = getStartOfWeek(LocalDate.now()) }
        )

        // 캘린더 본체 (타임라인 + 그리드 + 이벤트)
        CalendarBody(
            startOfWeek = currentWeekStart,
            events = events,
            onNewEvent = { newEvent ->
                // 새 이벤트를 리스트에 추가 (id는 임시로 현재 시간 사용)
                events = events + newEvent.copy(id = System.currentTimeMillis())
            },
            onEventClick = { clickedEvent ->
                // TODO: 이벤트 클릭 시 동작 구현 (예: 상세 다이얼로그)
            }
        )
    }
}


private fun getStartOfWeek(date: LocalDate): LocalDate {
    val weekFields = WeekFields.of(Locale.KOREA) // 한국 기준 (일요일 시작)
    return date.with(weekFields.dayOfWeek(), 1)
}

private fun getSampleEvents(): List<Event> {
    val today = LocalDate.now().atTime(9, 0)
    return listOf(
        Event(1, "팀 미팅", today, today.plusHours(1), EVENT_COLORS[0]),
        Event(2, "디자인 리뷰", today.plusHours(0).plusMinutes(30), today.plusHours(1).plusMinutes(30), EVENT_COLORS[1]),
        Event(3, "점심 식사", today.plusHours(3), today.plusHours(4), EVENT_COLORS[2]),
        Event(4, "프로젝트 마감", today.plusDays(1).withHour(14), today.plusDays(1).withHour(15).withMinute(30), EVENT_COLORS[3]),
        Event(5, "요가 클래스", today.minusDays(1).withHour(18), today.minusDays(1).withHour(19), EVENT_COLORS[4]),
        Event(6, "중첩 이벤트 1", today.plusHours(0), today.plusHours(2), EVENT_COLORS[5]),
        Event(7, "중첩 이벤트 2", today.plusHours(0), today.plusHours(1), EVENT_COLORS[6]),
        Event(8, "중첩 이벤트 3", today.plusHours(1).plusMinutes(30), today.plusHours(2).plusMinutes(30), EVENT_COLORS[7]),
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ADPTheme{
        WeeklyCalendarScreen()
    }
}



// --- 1. 캘린더 헤더 ---
@Composable
fun CalendarHeader(
    startOfWeek: LocalDate,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // 년/월 및 주 이동 버튼
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = startOfWeek.format(yearMonthFormatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3C4043)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onPrevWeek, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Gray)) { Text("이전 주") }
            Button(onClick = onToday, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black)) { Text("오늘") }
            Button(onClick = onNextWeek, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Gray)) { Text("다음 주") }
        }

        // 요일 및 날짜 헤더
        DayHeaders(startOfWeek = startOfWeek)
    }
}

@Composable
fun DayHeaders(startOfWeek: LocalDate) {
    val today = LocalDate.now()
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)) {
        // 타임라인 영역 (왼쪽 공백)
        Spacer(modifier = Modifier.width(60.dp))

        // 7일치 날짜
        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val isToday = date == today

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = DAYS_OF_WEEK[i],
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(36.dp)
                        .then(
                            if (isToday) Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                            else Modifier
                        )
                ) {
                    Text(
                        text = date.format(dayFormatter),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isToday) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

// --- 2. 캘린더 본체 (스크롤 영역) ---
@Composable
fun CalendarBody(
    startOfWeek: LocalDate,
    events: List<Event>,
    onNewEvent: (Event) -> Unit,
    onEventClick: (Event) -> Unit
) {
    val scrollState = rememberScrollState()

    // 드래그로 생성 중인 임시 이벤트
    var dragEvent by remember { mutableStateOf<Event?>(null) }

    // 캘린더 스크롤 영역 (Timeline + Grid + Events)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Row {
            // A. 타임라인 (00:00 - 23:00)
            Timeline()

            // B. 그리드 + 이벤트
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(HOUR_HEIGHT * 24) // 24시간 높이
            ) {
                // 그리드 배경 (Canvas)
                CalendarGrid(
                    startOfWeek = startOfWeek,
                    onDragStart = { _, startTime ->
                        dragEvent = Event(0, "새 일정", startTime, startTime.plusMinutes(30), EVENT_COLORS[0])
                    },
                    onDrag = { _, currentTime ->
                        dragEvent?.let {
                            if (currentTime.isAfter(it.start)) {
                                dragEvent = it.copy(end = currentTime)
                            }
                        }
                    },
                    onDragEnd = {
                        dragEvent?.let {
                            // 30분 미만이면 30분으로 설정
                            val finalEnd = if (ChronoUnit.MINUTES.between(it.start, it.end) < 30) {
                                it.start.plusMinutes(30)
                            } else {
                                it.end
                            }
                            onNewEvent(it.copy(end = finalEnd))
                        }
                        dragEvent = null
                    }
                )

                // 이벤트 블록 렌더링
                EventRenderer(
                    startOfWeek = startOfWeek,
                    events = events + listOfNotNull(dragEvent), // 드래그 중인 이벤트도 함께 렌더링
                    onEventClick = onEventClick
                )
            }
        }
    }
}

// --- 2-A. 타임라인 ---
@Composable
fun Timeline() {
    Column(modifier = Modifier.width(60.dp).padding(horizontal = 8.dp)) {
        for (i in 0..23) {
            Box(
                modifier = Modifier.height(HOUR_HEIGHT),
                contentAlignment = Alignment.TopCenter
            ) {
                if (i > 0) {
                    Text(
                        text = "${String.format("%02d", i)}:00",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.offset(y = (-8).dp) // 선 위에 걸치도록
                    )
                }
            }
        }
    }
}

// --- 2-B. 캘린더 그리드 (배경 및 드래그 처리) ---
@Composable
fun CalendarGrid(
    startOfWeek: LocalDate,
    onDragStart: (LocalDate, LocalDateTime) -> Unit,
    onDrag: (LocalDate, LocalDateTime) -> Unit,
    onDragEnd: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(startOfWeek) { // startOfWeek가 바뀌면 pointerInput 재설정
                detectDragGestures(
                    onDragStart = { offset ->
                        val dayWidth = size.width / 7f
                        val dayIndex = (offset.x / dayWidth).toInt().coerceIn(0, 6)
                        val dayDate = startOfWeek.plusDays(dayIndex.toLong())
                        val time = dpToLocalDateTime(offset.y.toDp(), dayDate)
                        onDragStart(dayDate, time)
                    },
                    onDrag = { change, _ ->
                        val dayWidth = size.width / 7f
                        val dayIndex = (change.position.x / dayWidth).toInt().coerceIn(0, 6)
                        val dayDate = startOfWeek.plusDays(dayIndex.toLong())
                        val time = dpToLocalDateTime(change.position.y.toDp(), dayDate)
                        onDrag(dayDate, time)
                    },
                    onDragEnd = { onDragEnd() }
                )
            }
    ) {
        val dayWidth = size.width / 7f
        val hourHeight = HOUR_HEIGHT.toPx()

        // 1. 가로줄 (시간)
        for (i in 1..23) {
            drawLine(
                color = GridLineColor,
                start = Offset(0f, i * hourHeight),
                end = Offset(size.width, i * hourHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. 세로줄 (날짜)
        for (i in 1..6) {
            drawLine(
                color = GridLineColor,
                start = Offset(i * dayWidth, 0f),
                end = Offset(i * dayWidth, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

// --- 2-C. 이벤트 렌더링 ---
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EventRenderer(
    startOfWeek: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val endOfWeek = startOfWeek.plusDays(7)

    // 1. 현재 주의 이벤트 필터링
    val weekEvents = events.filter {
        !it.start.isBefore(startOfWeek.atStartOfDay()) && it.start.isBefore(endOfWeek.atStartOfDay())
    }

    // 2. 날짜별(0-6)로 이벤트 그룹화
    val eventsByDay = remember(weekEvents, startOfWeek) {
        val groups: Array<MutableList<Event>> = Array(7) { mutableListOf() }
        weekEvents.forEach {
            val dayIndex = ChronoUnit.DAYS.between(startOfWeek, it.start.toLocalDate()).toInt()
            if (dayIndex in 0..6) {
                groups[dayIndex].add(it)
            }
        }
        // ★★★★★ 수정 지점 ★★★★★
        // .map의 결과를 반환하여 eventsByDay가 올바른 타입을 갖도록 합니다.
        groups.map { group -> group.sortedBy { event -> event.start } }
    }

    // --- Box와 absoluteOffset 사용 ---
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val dayWidth = maxWidth / 7

        eventsByDay.forEachIndexed { dayIndex, dayEvents ->
            if (dayEvents.isNotEmpty()) {
                val layouts = calculateEventLayouts(dayEvents)

                layouts.forEach { layout ->
                    val xOffset = dayWidth * dayIndex + (dayWidth * layout.left)
                    val eventWidth = dayWidth * layout.width

                    EventBlock(
                        event = layout.event,
                        layout = layout,
                        onClick = { onEventClick(layout.event) },
                        modifier = Modifier
                            .absoluteOffset(x = xOffset, y = layout.top)
                            .width(eventWidth - 4.dp) // 약간의 마진
                            .height(layout.height)
                            .zIndex(if (layout.event.id == 0L) 10f else 1f) // 드래그 중인 이벤트
                    )
                }
            }
        }
    }
}

@Composable
private fun EventBlock(
    event: Event,
    layout: EventLayout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDraggingPlaceholder = event.id == 0L

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isDraggingPlaceholder) event.color.copy(alpha = 0.3f)
                else event.color.copy(alpha = 0.8f)
            )
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = event.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (layout.height > 30.dp) { // 공간이 있을 때만 시간 표시
                Text(
                    text = "${event.start.format(timeFormatter)} - ${event.end.format(timeFormatter)}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


/**
 * ★★★ 핵심 중첩 계산 알고리즘 ★★★
 */
private fun calculateEventLayouts(dayEvents: List<Event>): List<EventLayout> {
    // colIndex 계산을 위해 MutableEventLayout 타입을 사용
    val layouts = mutableListOf<MutableEventLayout>()

    // 1. 이벤트마다 top, height 계산 및 colIndex 할당
    for (event in dayEvents) {
        val startMinutes = event.start.hour * 60L + event.start.minute
        val endMinutes = event.end.hour * 60L + event.end.minute

        val top = minutesToDp(startMinutes)
        val height = minutesToDp(endMinutes - startMinutes)

        // 이 이벤트와 겹치는 이벤트들 (layouts에 이미 추가된 것들 중)
        val collidingEvents = layouts.filter {
            val lStart = it.event.start.hour * 60L + it.event.start.minute
            val lEnd = it.event.end.hour * 60L + it.event.end.minute
            (startMinutes < lEnd && endMinutes > lStart)
        }

        // 겹치는 이벤트들의 '열(column)' 인덱스를 찾음 (0, 1, 2...)
        val occupiedColumns = collidingEvents.map { it.colIndex }.toSet()

        // 0부터 시작해서 비어있는 '열'을 찾음
        var colIndex = 0
        while (occupiedColumns.contains(colIndex)) {
            colIndex++
        }

        // 수정: .toEventLayout() 호출을 제거하여 colIndex 정보가 유지되도록 함
        layouts.add(MutableEventLayout(event, top, height, colIndex))
    }

    // 2. 중첩 그룹별로 left, width 계산
    val processedLayouts = mutableSetOf<MutableEventLayout>()
    val finalLayouts = mutableListOf<EventLayout>()

    layouts.forEach { layout ->
        if (processedLayouts.contains(layout)) return@forEach // 이미 처리됨

        // 이 layout과 겹치는 모든 이벤트를 layouts에서 찾음
        val overlapGroup = mutableSetOf<MutableEventLayout>()
        val queue = ArrayDeque<MutableEventLayout>()

        overlapGroup.add(layout)
        queue.add(layout)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            layouts.forEach { other ->
                if (!overlapGroup.contains(other)) {
                    val cStart = current.event.start.toLocalTime()
                    val cEnd = current.event.end.toLocalTime()
                    val oStart = other.event.start.toLocalTime()

                    val oEnd = other.event.end.toLocalTime()

                    if (cStart.isBefore(oEnd) && cEnd.isAfter(oStart)) {
                        overlapGroup.add(other)
                        queue.add(other)
                    }
                }
            }
        }

        processedLayouts.addAll(overlapGroup)

        // 이 중첩 그룹에서 최대 '열' 개수 (max(colIndex) + 1)
        val maxCols = (overlapGroup.map { it.colIndex }.maxOrNull() ?: 0) + 1
        val colWidth = 1.0f / maxCols // 1.0f (100%) / 중첩개수

        // 그룹 내 모든 이벤트의 left, width 설정
        overlapGroup.forEach { l ->
            l.left = l.colIndex * colWidth
            l.width = colWidth
            finalLayouts.add(l.toEventLayout()) // 최종 리스트에는 일반 EventLayout으로 변환하여 추가
        }
    }

    return finalLayouts
}

// colIndex를 임시로 저장하기 위한 Mutable 클래스
private class MutableEventLayout(
    event: Event,
    top: Dp,
    height: Dp,
    val colIndex: Int
) : EventLayout(event, top, height, 0f, 0f) {
    // 최종 EventLayout으로 변환하는 함수
    fun toEventLayout(): EventLayout = EventLayout(event, top, height, left, width)
}