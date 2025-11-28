package com.example.wop_calender_p

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import com.example.wop_calender_p.ui.theme.ADPTheme
import com.google.ai.client.generativeai.GenerativeModel
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
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// ==========================================
// [1] Room Database & Entity
// ==========================================
@Entity(tableName = "events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val start: String,
    val end: String,
    val color: Int,
    val location: String? = null,
    val placeStatus: String? = null,
    val aiAdvice: String? = null
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

@Database(entities = [CalendarEventEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}

// ==========================================
// [2] Hilt Module
// ==========================================
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "calendar_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(modelName = "gemini-1.5-pro-latest", apiKey = BuildConfig.GEMINI_API_KEY)
    }
}

// ==========================================
// [3] ViewModel
// ==========================================
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val dao: EventDao,
    private val generativeModel: GenerativeModel
) : ViewModel() {
    val events: Flow<List<Event>> = dao.getAllEvents().map { entities ->
        entities.map { entity ->
            Event(entity.id, entity.title, LocalDateTime.parse(entity.start), LocalDateTime.parse(entity.end), Color(entity.color), entity.location, entity.placeStatus, entity.aiAdvice)
        }
    }
    fun addEvent(event: Event) {
        viewModelScope.launch {
            dao.insertEvent(CalendarEventEntity(title = event.title, start = event.start.toString(), end = event.end.toString(), color = event.color.toArgb(), location = event.location, placeStatus = event.placeStatus, aiAdvice = event.aiAdvice))
        }
    }
    suspend fun askGemini(title: String, location: String?): String {
        return try {
            val prompt = "나는 '$title'이라는 일정을 '${location ?: ""}'에서 할 거야. 이 일정 준비물이나 주의할 점을 한국어로 아주 짧게 1문장으로 조언해줘. (존댓말)"
            val response = generativeModel.generateContent(prompt)
            response.text ?: "AI가 조언을 생각 중입니다..."
        } catch (e: Exception) { "AI 연결 실패: 인터넷을 확인하세요." }
    }
}

// ==========================================
// [4] UI & Logic Constants
// ==========================================
private val HOUR_HEIGHT = 64.dp
private val TIME_COLUMN_WIDTH = 60.dp
private val GridLineColor = Color(0xFFEEEEEE)
private val PurpleSelected = Color(0xFF5E5CE6)

private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")
private val dayFormatter = DateTimeFormatter.ofPattern("d")
private val dialogDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val dialogTimeFormatter = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREA)
private val DAYS_OF_WEEK = listOf("일", "월", "화", "수", "목", "금", "토")
private val EVENT_COLORS = listOf(
    Color(0xFF7986CB), Color(0xFF4DB6AC), Color(0xFFE57373), Color(0xFF9575CD),
    Color(0xFFF06292), Color(0xFFFF8A65), Color(0xFFAED581), Color(0xFF4DD0E1)
)

data class Event(val id: Long, val title: String, val start: LocalDateTime, val end: LocalDateTime, val color: Color, val location: String? = null, val placeStatus: String? = null, val aiAdvice: String? = null)

private fun getStartOfWeek(date: LocalDate): LocalDate = date.with(WeekFields.of(Locale.KOREA).dayOfWeek(), 1)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ADPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    WeeklyCalendarScreen()
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(LocalDate.now())) }
    val events by viewModel.events.collectAsState(initial = emptyList())
    var showAddEventDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        WeeklyHeader(
            currentWeekStart = currentWeekStart,
            onPrevClick = { currentWeekStart = currentWeekStart.minusWeeks(1) },
            onNextClick = { currentWeekStart = currentWeekStart.plusWeeks(1) },
            onTodayClick = { currentWeekStart = getStartOfWeek(LocalDate.now()) },
            onNewEventClick = { showAddEventDialog = true }
        )
        DayHeaders(startOfWeek = currentWeekStart)
        HorizontalDivider(thickness = 1.dp, color = GridLineColor)

        Row(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState)) {
            TimeSidebar()
            Box(modifier = Modifier.weight(1f).height(HOUR_HEIGHT * 24)) {
                CalendarGrid()
                EventRenderer(startOfWeek = currentWeekStart, events = events)
                CurrentTimeLine()
            }
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onSave = { viewModel.addEvent(it); showAddEventDialog = false },
            viewModel = viewModel
        )
    }
}

// [UI Components]

@Composable
fun WeeklyHeader(currentWeekStart: LocalDate, onPrevClick: () -> Unit, onNextClick: () -> Unit, onTodayClick: () -> Unit, onNewEventClick: () -> Unit) {
    val endOfWeek = currentWeekStart.plusDays(6)
    val weekRangeFormatter = remember { DateTimeFormatter.ofPattern("M.dd") }
    val weekRangeText = "${currentWeekStart.format(weekRangeFormatter)} - ${endOfWeek.format(weekRangeFormatter)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentWeekStart.format(yearMonthFormatter),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weekRangeText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GridLineColor),
                color = Color.White,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous week", tint = Color.DarkGray)
                    }
                    Text(
                        "오늘",
                        modifier = Modifier
                            .clickable(onClick = onTodayClick)
                            .padding(horizontal = 8.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    IconButton(onClick = onNextClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next week", tint = Color.DarkGray)
                    }
                }
            }

            Button(
                onClick = onNewEventClick,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleSelected),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Add, "New event", modifier = Modifier.size(24.dp), tint = Color.White)
            }
        }
    }
}

@Composable
fun DayHeaders(startOfWeek: LocalDate) {
    val today = LocalDate.now()
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Spacer(modifier = Modifier.width(TIME_COLUMN_WIDTH))
        for (i in 0..6) {
            val date = startOfWeek.plusDays(i.toLong())
            val isToday = date == today
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = DAYS_OF_WEEK[i], style = MaterialTheme.typography.labelMedium, color = if(isToday) PurpleSelected else Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (isToday) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(CircleShape).background(PurpleSelected)) {
                        Text(text = date.format(dayFormatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Text(text = date.format(dayFormatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun TimeSidebar() {
    Column(modifier = Modifier.width(TIME_COLUMN_WIDTH), horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in 0..23) {
            Box(modifier = Modifier.height(HOUR_HEIGHT).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                if (i > 0) Text(text = String.format(Locale.US, "%d %s", if(i > 12) i-12 else if (i == 0) 12 else i, if(i>=12) "PM" else "AM"), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.offset(y = (-8).dp))
            }
        }
    }
}

@Composable
fun CalendarGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dayWidth = size.width / 7f
        val hourHeight = HOUR_HEIGHT.toPx()
        for (i in 1..23) {
            drawLine(color = GridLineColor, start = Offset(0f, i * hourHeight), end = Offset(size.width, i * hourHeight), strokeWidth = 1.dp.toPx())
        }
        for (i in 1..6) {
            drawLine(color = GridLineColor, start = Offset(i * dayWidth, 0f), end = Offset(i * dayWidth, size.height), strokeWidth = 1.dp.toPx())
        }
    }
}

@Composable
fun CurrentTimeLine() {
    // TODO: Implement this later
}

@Composable
fun EventRenderer(startOfWeek: LocalDate, events: List<Event>) {
    // TODO: Implement this
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit,
    viewModel: CalendarViewModel
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var startTime by remember { mutableStateOf(LocalTime.now().withMinute(0).plusHours(1)) }
    var endTime by remember { mutableStateOf(LocalTime.now().withMinute(0).plusHours(2)) }
    var aiAdvice by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isSaveEnabled = title.isNotBlank() && endTime.isAfter(startTime)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("새 일정 만들기", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DialogSection(title = "일정 제목") {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("예: 팀 회의, 저녁 약속", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleSelected, unfocusedBorderColor = GridLineColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    DialogSection(title = "장소 (선택)") {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("어디서 하나요?", color = Color.LightGray) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleSelected, unfocusedBorderColor = GridLineColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    DialogSection(title = "날짜") {
                        PickerField(
                            text = date.format(dialogDateFormatter),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray) },
                            onClick = { showDatePicker = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            DialogSection(title = "시작 시간") {
                                PickerField(
                                    text = startTime.format(dialogTimeFormatter),
                                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray) },
                                    trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray) },
                                    onClick = { showStartTimePicker = true }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            DialogSection(title = "종료 시간") {
                                PickerField(
                                    text = endTime.format(dialogTimeFormatter),
                                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray) },
                                    trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray) },
                                    onClick = { showEndTimePicker = true }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = PurpleSelected, modifier = Modifier.size(20.dp))
                            Text("AI 조언 / 메모", fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = {
                            if (title.isNotBlank()) {
                                isAiLoading = true
                                scope.launch { aiAdvice = viewModel.askGemini(title, location.ifBlank { null }); isAiLoading = false }
                            }
                        }, enabled = !isAiLoading) {
                            Text("✨ AI 조언 받기", color = PurpleSelected, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = if(isAiLoading) "AI가 조언을 생성 중입니다..." else aiAdvice,
                        onValueChange = { aiAdvice = it },
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp),
                        placeholder = { Text("AI가 이 일정에 대한 팁이나 준비물을 알려줍니다.", color = Color.LightGray, fontSize = 14.sp) },
                        readOnly = isAiLoading,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurpleSelected, unfocusedBorderColor = GridLineColor)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newEvent = Event(0, title, LocalDateTime.of(date, startTime), LocalDateTime.of(date, endTime), EVENT_COLORS.random(), location.ifBlank { null }, aiAdvice = aiAdvice.ifBlank { null })
                            onSave(newEvent)
                        },
                        enabled = isSaveEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleSelected)
                    ) {
                        Text("저장", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker || showEndTimePicker) {
        val isStart = showStartTimePicker
        val initialTime = if (isStart) startTime else endTime
        val timePickerState = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute, is24Hour = false)

        TimePickerDialog(
            onDismiss = { showStartTimePicker = false; showEndTimePicker = false },
            onConfirm = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                if (isStart) {
                    startTime = newTime
                    if (!endTime.isAfter(newTime)) endTime = newTime.plusHours(1)
                } else {
                    endTime = newTime
                }
                showStartTimePicker = false
                showEndTimePicker = false
            },
            title = if (isStart) "시작 시간" else "종료 시간",
            content = { TimePicker(state = timePickerState) }
        )
    }
}

@Composable
fun DialogSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun PickerField(
    text: String,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GridLineColor),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                leadingIcon()
                Text(text, fontSize = 16.sp)
            }
            trailingIcon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { content() } },
        confirmButton = { TextButton(onClick = onConfirm) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
