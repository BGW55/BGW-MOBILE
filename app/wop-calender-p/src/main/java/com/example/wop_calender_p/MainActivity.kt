package com.example.wop_calender_p

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel // 👈 Hilt 전용 뷰모델 주입 함수
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import com.example.wop_calender_p.ui.theme.ADPTheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// ==========================================
// [1] Room Database & Entity (데이터 저장소)
// ==========================================

@Entity(tableName = "events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val start: String,
    val end: String,
    val color: Int,
    val location: String? = null,
    val placeStatus: String? = null
)

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert
    suspend fun insertEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)
}

@Database(entities = [CalendarEventEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}

// ==========================================
// [2] Hilt Module (의존성 주입 설정)
// ==========================================

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calendar_db"
        ).build()
    }

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao {
        return database.eventDao()
    }
}

// ==========================================
// [3] ViewModel (Hilt 적용)
// ==========================================

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val dao: EventDao
) : ViewModel() {

    val events: Flow<List<Event>> = dao.getAllEvents().map { entities ->
        entities.map { entity ->
            Event(
                id = entity.id,
                title = entity.title,
                start = LocalDateTime.parse(entity.start),
                end = LocalDateTime.parse(entity.end),
                color = Color(entity.color),
                location = entity.location,
                placeStatus = entity.placeStatus
            )
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            dao.insertEvent(
                CalendarEventEntity(
                    title = event.title,
                    start = event.start.toString(),
                    end = event.end.toString(),
                    color = event.color.toArgb(),
                    location = event.location,
                    placeStatus = event.placeStatus
                )
            )
        }
    }
}

// ==========================================
// [4] UI 및 로직
// ==========================================

private val HOUR_HEIGHT = 64.dp
private val GridLineColor = Color(0xFFEEEEEE)
private val CurrentTimeColor = Color(0xFFFF5252)
private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")
private val dayFormatter = DateTimeFormatter.ofPattern("d")
private val dialogDateFormatter = DateTimeFormatter.ofPattern("M월 d일 (E)")
private val dialogTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
private val DAYS_OF_WEEK = listOf("일", "월", "화", "수", "목", "금", "토")
private val EVENT_COLORS = listOf(
    Color(0xFF7986CB), Color(0xFF4DB6AC), Color(0xFFE57373), Color(0xFF9575CD),
    Color(0xFFF06292), Color(0xFFFF8A65), Color(0xFFAED581), Color(0xFF4DD0E1)
)

data class Event(
    val id: Long,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val color: Color,
    val location: String? = null,
    val placeStatus: String? = null
)

open class EventLayout(
    val event: Event,
    val top: Dp,
    val height: Dp,
    var left: Float,
    var width: Float
)

private fun Dp.toLocalDateTime(baseDate: LocalDate): LocalDateTime {
    val minutes = (this / HOUR_HEIGHT * 60).toLong()
    return baseDate.atStartOfDay().plusMinutes(minutes)
}

private fun Long.toCalendarDp(): Dp {
    return (this / 60f).toFloat().dp * HOUR_HEIGHT.value
}

@AndroidEntryPoint // 👈 Hilt를 쓰려면 이 어노테이션이 필수입니다!
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WeeklyCalendarScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyCalendarScreen(
    // ⬇️ Hilt가 ViewModel을 자동으로 찾아 넣어줍니다.
    viewModel: CalendarViewModel = hiltViewModel()
) {
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(LocalDate.now())) }
    val events by viewModel.events.collectAsState(initial = emptyList())
    var showAddEventDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentWeekStart.format(yearMonthFormatter),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 주")
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { currentWeekStart = getStartOfWeek(LocalDate.now()) },
                        modifier = Modifier.padding(end = 8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) { Text("오늘") }
                    IconButton(onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 주")
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
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "일정 추가")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            DayHeaders(startOfWeek = currentWeekStart)
            HorizontalDivider(thickness = 1.dp, color = GridLineColor)
            CalendarBody(
                startOfWeek = currentWeekStart,
                events = events,
                onNewEvent = { newEvent -> viewModel.addEvent(newEvent) },
                onEventClick = { }
            )
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onSave = { newEvent ->
                viewModel.addEvent(newEvent)
                showAddEventDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(onDismiss: () -> Unit, onSave: (Event) -> Unit) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var placeStatus by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var startTime by remember { mutableStateOf<LocalTime?>(LocalTime.now().truncatedTo(ChronoUnit.HOURS)) }
    var endTime by remember { mutableStateOf<LocalTime?>(LocalTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showLocationSearch by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(initialHour = startTime?.hour ?: 9, initialMinute = 0)
    val endTimePickerState = rememberTimePickerState(initialHour = endTime?.hour ?: 10, initialMinute = 0)

    val isRiskyStatus = placeStatus?.contains("브레이크") == true || placeStatus?.contains("종료") == true || placeStatus?.contains("휴무") == true
    val isSaveEnabled = title.isNotBlank() && date != null && startTime != null && endTime != null && (endTime?.isAfter(startTime) ?: false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("새 일정 만들기", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("일정 제목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = location, onValueChange = { location = it }, label = { Text("장소 (스마트 검색)") },
                    placeholder = { Text("장소 검색 버튼을 눌러보세요") },
                    trailingIcon = {
                        if (placeStatus != null) {
                            val isGood = placeStatus == "영업 중" || placeStatus == "예약 가능"
                            Surface(color = if(isGood) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(6.dp)) {
                                Text(text = placeStatus!!, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if(isGood) Color(0xFF2E7D32) else Color(0xFFC62828), modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                            }
                        }
                    }, readOnly = false, modifier = Modifier.fillMaxWidth()
                )
                FilledTonalButton(onClick = { showLocationSearch = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("지도에서 상태 확인하기 (Demo)")
                }
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(date?.format(dialogDateFormatter) ?: "날짜 선택") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showStartTimePicker = true }, modifier = Modifier.weight(1f)) { Text(startTime?.format(dialogTimeFormatter) ?: "시작") }
                    OutlinedButton(onClick = { showEndTimePicker = true }, modifier = Modifier.weight(1f)) { Text(endTime?.format(dialogTimeFormatter) ?: "종료") }
                }
                if (isRiskyStatus) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, "Warning", tint = MaterialTheme.colorScheme.onErrorContainer); Spacer(Modifier.width(8.dp))
                            Text(text = "주의: 선택하신 시간은 '$placeStatus' 시간대입니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newEvent = Event(0, title, LocalDateTime.of(date!!, startTime!!), LocalDateTime.of(date!!, endTime!!), EVENT_COLORS.random(), if(location.isBlank()) null else location, placeStatus)
                onSave(newEvent)
            }, enabled = isSaveEnabled) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }; showDatePicker = false }) { Text("확인") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }) { DatePicker(state = datePickerState) }
    }
    if (showStartTimePicker) TimePickerDialog(onDismiss = { showStartTimePicker = false }, onConfirm = { startTime = LocalTime.of(startTimePickerState.hour, startTimePickerState.minute); showStartTimePicker = false }, state = startTimePickerState)
    if (showEndTimePicker) TimePickerDialog(onDismiss = { showEndTimePicker = false }, onConfirm = { endTime = LocalTime.of(endTimePickerState.hour, endTimePickerState.minute); showEndTimePicker = false }, state = endTimePickerState)
    if (showLocationSearch) {
        AlertDialog(onDismissRequest = { showLocationSearch = false }, title = { Text("장소 검색") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { location = "스타벅스 강남점"; placeStatus = "영업 중"; showLocationSearch = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("스타벅스 (✅ 영업 중)") }; Button(onClick = { location = "유명 맛집"; placeStatus = "브레이크 타임"; showLocationSearch = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("맛집 (⚠️ 브레이크 타임)") } } }, confirmButton = {}, dismissButton = { TextButton(onClick = { showLocationSearch = false }) { Text("닫기") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, state: TimePickerState) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onConfirm) { Text("확인") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }, text = { TimePicker(state = state) })
}

@Composable
fun DayHeaders(startOfWeek: LocalDate) {
    val today = LocalDate.now()
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).background(MaterialTheme.colorScheme.surface)) {
        Spacer(modifier = Modifier.width(60.dp))
        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val isToday = date == today
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = DAYS_OF_WEEK[i], style = MaterialTheme.typography.labelMedium, color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp).shadow(if(isToday) 4.dp else 0.dp, CircleShape).clip(CircleShape).background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)) {
                    Text(text = date.format(dayFormatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun CalendarBody(startOfWeek: LocalDate, events: List<Event>, onNewEvent: (Event) -> Unit, onEventClick: (Event) -> Unit) {
    val scrollState = rememberScrollState()
    var dragEvent by remember { mutableStateOf<Event?>(null) }
    Box(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
        Row {
            Timeline()
            Box(modifier = Modifier.weight(1f).height(HOUR_HEIGHT * 24)) {
                CalendarGrid(
                    startOfWeek = startOfWeek,
                    onDragStart = { _, startTime -> dragEvent = Event(0, "새 일정", startTime, startTime.plusMinutes(60), EVENT_COLORS[0]) },
                    onDrag = { _, currentTime -> dragEvent?.let { if (currentTime.isAfter(it.start)) dragEvent = it.copy(end = currentTime) } },
                    onDragEnd = { dragEvent?.let { val finalEnd = if (ChronoUnit.MINUTES.between(it.start, it.end) < 30) it.start.plusMinutes(30) else it.end; onNewEvent(it.copy(end = finalEnd)) }; dragEvent = null }
                )
                EventRenderer(startOfWeek = startOfWeek, events = events + listOfNotNull(dragEvent), onEventClick = onEventClick)
                CurrentTimeLine(startOfWeek = startOfWeek)
            }
        }
    }
}

@Composable
fun CurrentTimeLine(startOfWeek: LocalDate) {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val endOfWeek = startOfWeek.plusDays(7)
    if (!today.isBefore(startOfWeek) && today.isBefore(endOfWeek)) {
        val dayIndex = ChronoUnit.DAYS.between(startOfWeek, today).toInt()
        val timeOffset = (now.hour * 60L + now.minute).toCalendarDp()
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dayWidth = size.width / 7f; val y = timeOffset.toPx(); val xStart = dayIndex * dayWidth; val xEnd = xStart + dayWidth
            drawCircle(color = CurrentTimeColor, radius = 5.dp.toPx(), center = Offset(xStart, y))
            drawLine(color = CurrentTimeColor, start = Offset(xStart, y), end = Offset(xEnd, y), strokeWidth = 2.dp.toPx())
        }
    }
}

@Composable
fun Timeline() {
    Column(modifier = Modifier.width(60.dp).padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in 0..23) {
            Box(modifier = Modifier.height(HOUR_HEIGHT), contentAlignment = Alignment.TopCenter) {
                if (i > 0) Text(text = "$i", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.offset(y = (-8).dp))
            }
        }
    }
}

@Composable
fun CalendarGrid(startOfWeek: LocalDate, onDragStart: (LocalDate, LocalDateTime) -> Unit, onDrag: (LocalDate, LocalDateTime) -> Unit, onDragEnd: () -> Unit) {
    Canvas(modifier = Modifier.fillMaxSize().pointerInput(startOfWeek) { detectDragGestures(onDragStart = { offset -> val dayWidth = size.width / 7f; val dayIndex = (offset.x / dayWidth).toInt().coerceIn(0, 6); val dayDate = startOfWeek.plusDays(dayIndex.toLong()); val time = offset.y.toDp().toLocalDateTime(dayDate); onDragStart(dayDate, time) }, onDrag = { change, _ -> val dayWidth = size.width / 7f; val dayIndex = (change.position.x / dayWidth).toInt().coerceIn(0, 6); val dayDate = startOfWeek.plusDays(dayIndex.toLong()); val time = change.position.y.toDp().toLocalDateTime(dayDate); onDrag(dayDate, time) }, onDragEnd = { onDragEnd() }) }) {
        val dayWidth = size.width / 7f; val hourHeight = HOUR_HEIGHT.toPx()
        for (i in 1..23) drawLine(color = GridLineColor, start = Offset(0f, i * hourHeight), end = Offset(size.width, i * hourHeight), strokeWidth = 1.dp.toPx())
        for (i in 1..6) drawLine(color = GridLineColor, start = Offset(i * dayWidth, 0f), end = Offset(i * dayWidth, size.height), strokeWidth = 1.dp.toPx())
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EventRenderer(startOfWeek: LocalDate, events: List<Event>, onEventClick: (Event) -> Unit) {
    val endOfWeek = startOfWeek.plusDays(7)
    val weekEvents = events.filter { !it.start.isBefore(startOfWeek.atStartOfDay()) && it.start.isBefore(endOfWeek.atStartOfDay()) }
    val eventsByDay = remember(weekEvents, startOfWeek) { val groups: Array<MutableList<Event>> = Array(7) { mutableListOf() }; weekEvents.forEach { val dayIndex = ChronoUnit.DAYS.between(startOfWeek, it.start.toLocalDate()).toInt(); if (dayIndex in 0..6) groups[dayIndex].add(it) }; groups.map { group -> group.sortedBy { event -> event.start } } }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val dayWidth = maxWidth / 7
        eventsByDay.forEachIndexed { dayIndex, dayEvents -> if (dayEvents.isNotEmpty()) { val layouts = calculateEventLayouts(dayEvents); layouts.forEach { layout -> EventBlock(event = layout.event, layout = layout, onClick = { onEventClick(layout.event) }, modifier = Modifier.absoluteOffset(x = dayWidth * dayIndex + (dayWidth * layout.left), y = layout.top).width(dayWidth * layout.width - 2.dp).height(layout.height).zIndex(if (layout.event.id == 0L) 10f else 1f)) } } }
    }
}

@Composable
private fun EventBlock(event: Event, layout: EventLayout, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(event.color.copy(alpha = 0.9f)).border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).clickable { if (event.location != null) { val gmmIntentUri = Uri.parse("geo:0,0?q=${event.location}"); val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri); mapIntent.setPackage("com.google.android.apps.maps"); try { context.startActivity(mapIntent) } catch (e: Exception) { onClick() } } else { onClick() } }.padding(4.dp)) {
        Column {
            Text(text = event.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (event.location != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(10.dp)); Spacer(Modifier.width(2.dp))
                    Text(text = event.location, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Color.White.copy(alpha = 0.95f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (event.placeStatus != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(color = if(event.placeStatus == "영업 중") Color(0xFF66BB6A) else Color(0xFFEF5350), shape = RoundedCornerShape(4.dp), shadowElevation = 2.dp) {
                        Text(text = event.placeStatus, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
            } else if (layout.height > 30.dp) {
                Text(text = "${event.start.format(DateTimeFormatter.ofPattern("H:mm"))}", style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

private fun calculateEventLayouts(dayEvents: List<Event>): List<EventLayout> {
    val layouts = mutableListOf<MutableEventLayout>()
    for (event in dayEvents) {
        val startMinutes = event.start.hour * 60L + event.start.minute
        val endMinutes = event.end.hour * 60L + event.end.minute
        val top = startMinutes.toCalendarDp()
        val height = (endMinutes - startMinutes).toCalendarDp()
        val collidingEvents = layouts.filter { val lStart = it.event.start.hour * 60L + it.event.start.minute; val lEnd = it.event.end.hour * 60L + it.event.end.minute; (startMinutes < lEnd && endMinutes > lStart) }
        val occupiedColumns = collidingEvents.map { it.colIndex }.toSet()
        var colIndex = 0; while (occupiedColumns.contains(colIndex)) colIndex++
        layouts.add(MutableEventLayout(event, top, height, colIndex))
    }
    val finalLayouts = mutableListOf<EventLayout>()
    val processedLayouts = mutableSetOf<MutableEventLayout>()
    layouts.forEach { layout ->
        if (processedLayouts.contains(layout)) return@forEach
        val overlapGroup = mutableSetOf<MutableEventLayout>()
        val queue = ArrayDeque<MutableEventLayout>()
        overlapGroup.add(layout); queue.add(layout)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            layouts.forEach { other -> if (!overlapGroup.contains(other)) { val cStart = current.event.start.toLocalTime(); val cEnd = current.event.end.toLocalTime(); val oStart = other.event.start.toLocalTime(); val oEnd = other.event.end.toLocalTime(); if (cStart.isBefore(oEnd) && cEnd.isAfter(oStart)) { overlapGroup.add(other); queue.add(other) } } }
        }
        processedLayouts.addAll(overlapGroup)
        val maxCols = (overlapGroup.map { it.colIndex }.maxOrNull() ?: 0) + 1; val colWidth = 1.0f / maxCols
        overlapGroup.forEach { l -> l.left = l.colIndex * colWidth; l.width = colWidth; finalLayouts.add(l.toEventLayout()) }
    }
    return finalLayouts
}

private fun getStartOfWeek(date: LocalDate): LocalDate { val weekFields = WeekFields.of(Locale.KOREA); return date.with(weekFields.dayOfWeek(), 1) }
private fun getSampleEvents(): List<Event> { return emptyList() }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() { ADPTheme { } }

private class MutableEventLayout(event: Event, top: Dp, height: Dp, val colIndex: Int) : EventLayout(event, top, height, 0f, 0f) { fun toEventLayout(): EventLayout = EventLayout(event, top, height, left, width) }