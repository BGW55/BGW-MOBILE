package com.example.wop_calender

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
import com.example.wop_calender.ui.theme.ADPTheme
import com.example.wop_calender.ui.theme.WopcalenderTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

// --- 상수 정의 ---
private val HOUR_HEIGHT = 60.dp
private val GridLineColor = Color.LightGray.copy(alpha = 0.5f)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")
private val dayFormatter = DateTimeFormatter.ofPattern("d")
private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val DAYS_OF_WEEK = listOf("일", "월", "화", "수", "목", "금", "토")
private val EVENT_COLORS = listOf(
    Color(0xFFF4511E), Color(0xFF0B8043), Color(0xFF3367D6), Color(0xFF8E24AA),
    Color(0xFFE67C73), Color(0xFFF6BF26), Color(0xFF009688), Color(0xFFD50000)
)

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
    val minutes = ((offsetY.value / HOUR_HEIGHT.value) * 60).toLong()
    return date.atStartOfDay().plusMinutes(minutes)
}

private fun minutesToDp(minutes: Long): Dp {
    return ((minutes / 60f) * HOUR_HEIGHT.value).dp
}

// --- MainActivity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeeklyCalendarScreen()
                }
            }
        }
    }
}

// --- 메인 화면 Composable ---
@Composable
fun WeeklyCalendarScreen() {
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(LocalDate.now())) }
    var events by remember { mutableStateOf(getSampleEvents()) }

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader(
            startOfWeek = currentWeekStart,
            onPrevWeek = { currentWeekStart = currentWeekStart.minusWeeks(1) },
            onNextWeek = { currentWeekStart = currentWeekStart.plusWeeks(1) },
            onToday = { currentWeekStart = getStartOfWeek(LocalDate.now()) }
        )

        CalendarBody(
            startOfWeek = currentWeekStart,
            events = events,
            onNewEvent = { newEvent ->
                events = events + newEvent.copy(id = System.currentTimeMillis())
            },
            onEventClick = { clickedEvent ->
                // TODO: 이벤트 클릭 시 동작 구현
            }
        )
    }
}

private fun getStartOfWeek(date: LocalDate): LocalDate {
    val weekFields = WeekFields.of(Locale.KOREA)
    return date.with(weekFields.dayOfWeek(), 1)
}

private fun getSampleEvents(): List<Event> {
    val today = LocalDate.now().atTime(9, 0)
    return listOf(
        Event(1, "팀 미팅", today, today.plusHours(1), EVENT_COLORS[0]),
        Event(2, "디자인 리뷰", today.plusMinutes(30), today.plusHours(1).plusMinutes(30), EVENT_COLORS[1]),
        Event(3, "점심 식사", today.plusHours(3), today.plusHours(4), EVENT_COLORS[2]),
        Event(4, "프로젝트 마감", today.plusDays(1).withHour(14), today.plusDays(1).withHour(15).withMinute(30), EVENT_COLORS[3]),
        Event(5, "요가 클래스", today.minusDays(1).withHour(18), today.minusDays(1).withHour(19), EVENT_COLORS[4]),
        Event(6, "중첩 이벤트 1", today, today.plusHours(2), EVENT_COLORS[5]),
        Event(7, "중첩 이벤트 2", today, today.plusHours(1), EVENT_COLORS[6]),
        Event(8, "중첩 이벤트 3", today.plusHours(1).plusMinutes(30), today.plusHours(2).plusMinutes(30), EVENT_COLORS[7]),
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ADPTheme {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = startOfWeek.format(yearMonthFormatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3C4043)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onPrevWeek,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                )
            ) { Text("이전 주") }
            Button(
                onClick = onToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) { Text("오늘") }
            Button(
                onClick = onNextWeek,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Gray
                )
            ) { Text("다음 주") }
        }

        DayHeaders(startOfWeek = startOfWeek)
    }
}

@Composable
fun DayHeaders(startOfWeek: LocalDate) {
    val today = LocalDate.now()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Spacer(modifier = Modifier.width(60.dp))

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
                            if (isToday) Modifier.background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
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

// --- 2. 캘린더 본체 ---
@Composable
fun CalendarBody(
    startOfWeek: LocalDate,
    events: List<Event>,
    onNewEvent: (Event) -> Unit,
    onEventClick: (Event) -> Unit
) {
    val scrollState = rememberScrollState()
    var dragEvent by remember { mutableStateOf<Event?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Row {
            Timeline()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(HOUR_HEIGHT * 24)
            ) {
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

                EventRenderer(
                    startOfWeek = startOfWeek,
                    events = events + listOfNotNull(dragEvent),
                    onEventClick = onEventClick
                )
            }
        }
    }
}

// --- 2-A. 타임라인 ---
@Composable
fun Timeline() {
    Column(
        modifier = Modifier
            .width(60.dp)
            .padding(horizontal = 8.dp)
    ) {
        for (i in 0..23) {
            Box(
                modifier = Modifier.height(HOUR_HEIGHT),
                contentAlignment = Alignment.TopCenter
            ) {
                if (i > 0) {
                    Text(
                        text = String.format("%02d:00", i),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                }
            }
        }
    }
}

// --- 2-B. 캘린더 그리드 ---
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
            .pointerInput(startOfWeek) {
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
                        val dayIndex = (change.position.x / dayWidth)
                            .toInt()
                            .coerceIn(0, 6)
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

        for (i in 1..23) {
            drawLine(
                color = GridLineColor,
                start = Offset(0f, i * hourHeight),
                end = Offset(size.width, i * hourHeight),
                strokeWidth = 1.dp.toPx()
            )
        }

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

    val weekEvents = events.filter {
        !it.start.isBefore(startOfWeek.atStartOfDay()) &&
                it.start.isBefore(endOfWeek.atStartOfDay())
    }

    val eventsByDay = remember(weekEvents, startOfWeek) {
        val groups: Array<MutableList<Event>> = Array(7) { mutableListOf() }
        weekEvents.forEach {
            val dayIndex = ChronoUnit.DAYS.between(startOfWeek, it.start.toLocalDate()).toInt()
            if (dayIndex in 0..6) {
                groups[dayIndex].add(it)
            }
        }
        groups.map { group -> group.sortedBy { event -> event.start } }
    }

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
                            .width(eventWidth - 4.dp)
                            .height(layout.height)
                            .zIndex(if (layout.event.id == 0L) 10f else 1f)
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
            if (layout.height > 30.dp) {
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
 * 핵심 중첩 계산 알고리즘
 */
private fun calculateEventLayouts(dayEvents: List<Event>): List<EventLayout> {
    val layouts = mutableListOf<MutableEventLayout>()

    for (event in dayEvents) {
        val startMinutes = event.start.hour * 60L + event.start.minute
        val endMinutes = event.end.hour * 60L + event.end.minute

        val top = minutesToDp(startMinutes)
        val height = minutesToDp(endMinutes - startMinutes)

        val collidingEvents = layouts.filter {
            val lStart = it.event.start.hour * 60L + it.event.start.minute
            val lEnd = it.event.end.hour * 60L + it.event.end.minute
            (startMinutes < lEnd && endMinutes > lStart)
        }

        val occupiedColumns = collidingEvents.map { it.colIndex }.toSet()

        var colIndex = 0
        while (occupiedColumns.contains(colIndex)) {
            colIndex++
        }

        layouts.add(MutableEventLayout(event, top, height, colIndex))
    }

    val processedLayouts = mutableSetOf<MutableEventLayout>()
    val finalLayouts = mutableListOf<EventLayout>()

    layouts.forEach { layout ->
        if (processedLayouts.contains(layout)) return@forEach

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

        val maxCols = (overlapGroup.maxOfOrNull { it.colIndex } ?: 0) + 1
        val colWidth = 1.0f / maxCols

        overlapGroup.forEach { l ->
            l.left = l.colIndex * colWidth
            l.width = colWidth
            finalLayouts.add(l.toEventLayout())
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
    fun toEventLayout(): EventLayout = EventLayout(event, top, height, left, width)
}