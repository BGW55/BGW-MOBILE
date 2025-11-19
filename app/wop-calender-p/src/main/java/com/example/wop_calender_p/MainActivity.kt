package com.example.wop_calender_p

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.wop_calender_p.ui.theme.ADPTheme
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

// --- 상수 정의 ---
private val HOUR_HEIGHT = 64.dp // 터치 영역 확보를 위해 조금 더 키움
private val GridLineColor = Color(0xFFE0E0E0) // 더 은은한 회색
private val CurrentTimeColor = Color(0xFFFF5252) // 현재 시간 표시선 (빨강)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")
private val dayFormatter = DateTimeFormatter.ofPattern("d")
private val dialogDateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)")
private val dialogTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
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

open class EventLayout(
    val event: Event,
    val top: Dp,
    val height: Dp,
    var left: Float,
    var width: Float
)

// --- 헬퍼 함수 ---
private fun dpToLocalDateTime(offsetY: Dp, date: LocalDate): LocalDateTime {
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
            ADPTheme {
                // 시스템 바 색상과 어울리도록 Surface 설정
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyCalendarScreen() {
    // 1. 상태 관리
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(LocalDate.now())) }
    var events by remember { mutableStateOf(getSampleEvents()) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // 2. UI 구조 (Scaffold 적용)
    Scaffold(
        topBar = {
            // ✨ 직관적인 상단 앱 바 (TopAppBar)
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentWeekStart.format(yearMonthFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "이전 주"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { currentWeekStart = getStartOfWeek(LocalDate.now()) }) {
                        Text("오늘", fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "다음 주"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape // 둥근 FAB
            ) {
                Icon(Icons.Default.Add, contentDescription = "일정 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 요일 헤더 (고정)
            DayHeaders(startOfWeek = currentWeekStart)

            // 구분선
            HorizontalDivider(thickness = 1.dp, color = GridLineColor)

            // 스크롤 가능한 캘린더 본문
            CalendarBody(
                startOfWeek = currentWeekStart,
                events = events,
                onNewEvent = { newEvent ->
                    events = events + newEvent.copy(id = System.currentTimeMillis())
                },
                onEventClick = { /* TODO: 상세 보기 */ }
            )
        }
    }

    // 3. 일정 추가 다이얼로그
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onSave = { newEvent ->
                events = events + newEvent
                showAddEventDialog = false
            }
        )
    }
}

// --- 다이얼로그 (이전 코드 유지 + remember 위치 수정 적용됨) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) } // 기본값: 오늘
    var startTime by remember { mutableStateOf<LocalTime?>(LocalTime.now().truncatedTo(ChronoUnit.HOURS)) } // 기본값: 현재 시각(정각)
    var endTime by remember { mutableStateOf<LocalTime?>(LocalTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1)) } // 기본값: 1시간 뒤

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(initialHour = startTime?.hour ?: 9, initialMinute = 0)
    val endTimePickerState = rememberTimePickerState(initialHour = endTime?.hour ?: 10, initialMinute = 0)

    val isSaveEnabled = title.isNotBlank() && date != null && startTime != null && endTime != null && (endTime?.isAfter(startTime) ?: false)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
        title = { Text("새 일정 만들기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("일정 제목") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // 날짜 및 시간 선택 버튼 그룹
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = date?.format(dialogDateFormatter) ?: "날짜 선택", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(startTime?.format(dialogTimeFormatter) ?: "시작", color = MaterialTheme.colorScheme.onSurface)
                        }
                        OutlinedButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(endTime?.format(dialogTimeFormatter) ?: "종료", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (startTime != null && endTime != null && !endTime!!.isAfter(startTime)) {
                    Text("종료 시간은 시작 시간보다 늦어야 합니다.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newEvent = Event(
                        id = System.currentTimeMillis(),
                        title = title,
                        start = LocalDateTime.of(date!!, startTime!!),
                        end = LocalDateTime.of(date!!, endTime!!),
                        color = EVENT_COLORS.random()
                    )
                    onSave(newEvent)
                },
                enabled = isSaveEnabled
            ) { Text("저장") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                startTime = LocalTime.of(startTimePickerState.hour, startTimePickerState.minute)
                showStartTimePicker = false
            },
            state = startTimePickerState
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                endTime = LocalTime.of(endTimePickerState.hour, endTimePickerState.minute)
                showEndTimePicker = false
            },
            state = endTimePickerState
        )
    }
}

// 시간 선택 다이얼로그 래퍼 (재사용성을 위해 분리)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    state: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        text = { TimePicker(state = state) }
    )
}


// --- 헤더 UI 개선 ---
@Composable
fun DayHeaders(startOfWeek: LocalDate) {
    val today = LocalDate.now()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp) // 상하 여백 추가
    ) {
        Spacer(modifier = Modifier.width(60.dp)) // 타임라인 공간

        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val isToday = date == today

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = DAYS_OF_WEEK[i],
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))

                // 오늘 날짜면 배경색 추가
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                ) {
                    Text(
                        text = date.format(dayFormatter),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// --- 캘린더 본체 ---
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
                        dragEvent = Event(0, "새 일정", startTime, startTime.plusMinutes(60), EVENT_COLORS[0])
                    },
                    onDrag = { _, currentTime ->
                        dragEvent?.let {
                            if (currentTime.isAfter(it.start)) dragEvent = it.copy(end = currentTime)
                        }
                    },
                    onDragEnd = {
                        dragEvent?.let {
                            val finalEnd = if (ChronoUnit.MINUTES.between(it.start, it.end) < 30) it.start.plusMinutes(30) else it.end
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

                // ✨ 현재 시간 표시선 (Current Time Indicator) 추가
                CurrentTimeLine(startOfWeek = startOfWeek)
            }
        }
    }
}

// --- 현재 시간 표시선 Composable ---
@Composable
fun CurrentTimeLine(startOfWeek: LocalDate) {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val endOfWeek = startOfWeek.plusDays(7)

    // 오늘이 현재 주간에 포함되어 있는지 확인
    if (!today.isBefore(startOfWeek) && today.isBefore(endOfWeek)) {
        val dayIndex = ChronoUnit.DAYS.between(startOfWeek, today).toInt()
        val timeOffset = minutesToDp(now.hour * 60L + now.minute)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val dayWidth = size.width / 7f
            val y = timeOffset.toPx()
            val xStart = dayIndex * dayWidth
            val xEnd = xStart + dayWidth

            // 빨간색 점
            drawCircle(
                color = CurrentTimeColor,
                radius = 4.dp.toPx(),
                center = Offset(xStart, y)
            )
            // 빨간색 선
            drawLine(
                color = CurrentTimeColor,
                start = Offset(xStart, y),
                end = Offset(xEnd, y),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun Timeline() {
    Column(
        modifier = Modifier
            .width(60.dp)
            .padding(vertical = 8.dp), // 상단 여백 미세 조정
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (i in 0..23) {
            Box(modifier = Modifier.height(HOUR_HEIGHT), contentAlignment = Alignment.TopCenter) {
                // 1시, 2시... (0시는 보통 생략하거나 작게 표시)
                if (i > 0) {
                    Text(
                        text = "$i", // 간단하게 숫자만 표시하여 깔끔하게
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                }
            }
        }
    }
}

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

        // 가로줄 (시간)
        for (i in 1..23) {
            drawLine(
                color = GridLineColor,
                start = Offset(0f, i * hourHeight),
                end = Offset(size.width, i * hourHeight),
                strokeWidth = 1.dp.toPx()
            )
        }
        // 세로줄 (날짜)
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

// --- 이벤트 렌더링 (알고리즘 유지) ---
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EventRenderer(
    startOfWeek: LocalDate,
    events: List<Event>,
    onEventClick: (Event) -> Unit
) {
    val endOfWeek = startOfWeek.plusDays(7)
    val weekEvents = events.filter {
        !it.start.isBefore(startOfWeek.atStartOfDay()) && it.start.isBefore(endOfWeek.atStartOfDay())
    }

    val eventsByDay = remember(weekEvents, startOfWeek) {
        val groups: Array<MutableList<Event>> = Array(7) { mutableListOf() }
        weekEvents.forEach {
            val dayIndex = ChronoUnit.DAYS.between(startOfWeek, it.start.toLocalDate()).toInt()
            if (dayIndex in 0..6) groups[dayIndex].add(it)
        }
        groups.map { group -> group.sortedBy { event -> event.start } }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val dayWidth = maxWidth / 7

        eventsByDay.forEachIndexed { dayIndex, dayEvents ->
            if (dayEvents.isNotEmpty()) {
                val layouts = calculateEventLayouts(dayEvents)
                layouts.forEach { layout ->
                    EventBlock(
                        event = layout.event,
                        layout = layout,
                        onClick = { onEventClick(layout.event) },
                        modifier = Modifier
                            .absoluteOffset(
                                x = dayWidth * dayIndex + (dayWidth * layout.left),
                                y = layout.top
                            )
                            .width(dayWidth * layout.width - 2.dp) // 여백을 주어 이벤트 간 구분 명확히
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp)) // 둥근 모서리
            .background(event.color.copy(alpha = 0.85f)) // 약간의 투명도
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // 공간이 충분할 때만 시간 표시
            if (layout.height > 24.dp) {
                Text(
                    text = "${event.start.format(DateTimeFormatter.ofPattern("H:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- 알고리즘 (기존 유지) ---
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
        while (occupiedColumns.contains(colIndex)) colIndex++
        layouts.add(MutableEventLayout(event, top, height, colIndex))
    }

    val finalLayouts = mutableListOf<EventLayout>()
    val processedLayouts = mutableSetOf<MutableEventLayout>()

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
        val maxCols = (overlapGroup.map { it.colIndex }.maxOrNull() ?: 0) + 1
        val colWidth = 1.0f / maxCols
        overlapGroup.forEach { l ->
            l.left = l.colIndex * colWidth
            l.width = colWidth
            finalLayouts.add(l.toEventLayout())
        }
    }
    return finalLayouts
}

private fun getStartOfWeek(date: LocalDate): LocalDate {
    val weekFields = WeekFields.of(Locale.KOREA)
    return date.with(weekFields.dayOfWeek(), 1)
}

private fun getSampleEvents(): List<Event> {
    val today = LocalDate.now().atTime(9, 0)
    return listOf(
        Event(1, "팀 미팅", today, today.plusHours(1), EVENT_COLORS[0]),
        Event(2, "점심", today.plusHours(3), today.plusHours(4), EVENT_COLORS[2]),
        Event(3, "프로젝트 마감", today.plusDays(1).withHour(14), today.plusDays(1).withHour(16), EVENT_COLORS[3])
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ADPTheme { WeeklyCalendarScreen() }
}

// Private helper class
private class MutableEventLayout(
    event: Event, top: Dp, height: Dp, val colIndex: Int
) : EventLayout(event, top, height, 0f, 0f) {
    fun toEventLayout(): EventLayout = EventLayout(event, top, height, left, width)
}