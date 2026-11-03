package vn.phong.socongthang.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar
import vn.phong.socongthang.data.DayState
import vn.phong.socongthang.data.Job
import vn.phong.socongthang.data.dateKey

val JOB_COLORS = listOf(
    Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFFEF6C00),
    Color(0xFF6A1B9A), Color(0xFFC62828), Color(0xFF00838F)
)

fun jobColor(job: Job): Color = JOB_COLORS[job.colorIndex % JOB_COLORS.size]

fun daysInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun firstOffset(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    return (dow - Calendar.MONDAY + 7) % 7
}

@Composable
fun LanguageScreen(onChoose: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Choose language / Chọn ngôn ngữ",
            fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Pick once, the app will keep using it. You can change it later in the Projects tab.\nChọn một lần, app sẽ dùng luôn ngôn ngữ đó. Đổi lại sau trong tab Dự án.",
            fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onChoose("vi") },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tiếng Việt", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Vietnamese", fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onChoose("en") },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("English", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Tiếng Anh", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MonthNav(
    s: AppStrings,
    year: Int,
    month: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayMonth: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onPrevMonth, modifier = Modifier.size(40.dp, 36.dp)) { Text("<", fontSize = 16.sp) }
            Text(
                s.monthTitle(year, month),
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
            OutlinedButton(onClick = onNextMonth, modifier = Modifier.size(40.dp, 36.dp)) { Text(">", fontSize = 16.sp) }
            TextButton(onClick = onTodayMonth) { Text(s.backToday, fontSize = 11.sp) }
        }
    }
}

@Composable
fun CalendarScreen(
    s: AppStrings,
    jobs: List<Job>,
    days: Map<String, DayState>,
    year: Int,
    month: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayMonth: () -> Unit,
    onDayClick: (Int, Int, Int) -> Unit
) {
    val jobByNumber = jobs.associateBy { it.number }
    val todayCal = Calendar.getInstance()
    val todayKey = dateKey(
        todayCal.get(Calendar.YEAR),
        todayCal.get(Calendar.MONTH),
        todayCal.get(Calendar.DAY_OF_MONTH)
    )
    val todayState = days[todayKey]
    val showTodayBanner = todayState != null && !todayState.done &&
        (todayState.scheduled || todayState.jobNumbers.isNotEmpty())

    // Fits exactly one screen: no scroll, compact centered header,
    // small month bar, and the calendar card takes all remaining height.
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            s.appName, fontSize = 21.sp, fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20), modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            s.appNote, fontSize = 12.sp, color = Color.Gray,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        if (showTodayBanner) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    onDayClick(
                        todayCal.get(Calendar.YEAR),
                        todayCal.get(Calendar.MONTH),
                        todayCal.get(Calendar.DAY_OF_MONTH)
                    )
                }
            ) {
                Text(
                    s.todayNotDone,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Color(0xFFE65100), fontSize = 12.sp, textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        MonthNav(s, year, month, onPrevMonth, onNextMonth, onTodayMonth)
        Spacer(Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (w in s.weekdays) {
                        Text(w, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(6.dp))
                val total = daysInMonth(year, month)
                val cells = mutableListOf<Int?>()
                repeat(firstOffset(year, month)) { cells.add(null) }
                for (d in 1..total) cells.add(d)
                while (cells.size % 7 != 0) cells.add(null)
                for (week in cells.chunked(7)) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        for (day in week) {
                            if (day == null) {
                                Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(2.dp))
                            } else {
                                val key = dateKey(year, month, day)
                                val state = days[key]
                                val isToday = key == todayKey
                                val bg = when {
                                    state?.done == true -> Color(0xFFDCEDC8)
                                    state?.scheduled == true -> Color(0xFFFFE0B2)
                                    else -> Color(0xFFF8FFF8)
                                }
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxHeight().padding(2.dp)
                                        .shadow(2.dp, RoundedCornerShape(12.dp))
                                        .background(bg, RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isToday) 2.dp else 0.dp,
                                            color = if (isToday) Color(0xFF2E7D32) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onDayClick(year, month, day) }
                                        .padding(4.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$day", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            if (state?.done == true) {
                                                Text(" ✓", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            } else if (state?.scheduled == true) {
                                                Text(s.scheduledMarker(), fontSize = 9.sp, color = Color(0xFFE65100))
                                            }
                                        }
                                        if (state != null && state.jobNumbers.isNotEmpty()) {
                                            Spacer(Modifier.height(2.dp))
                                            val nums = state.jobNumbers.sorted()
                                            for (rowNums in nums.chunked(2)) {
                                                Row {
                                                    for (n in rowNums) {
                                                        val job = jobByNumber[n]
                                                        val c = if (job != null) jobColor(job) else Color.Gray
                                                        Box(
                                                            modifier = Modifier.size(18.dp).padding(1.dp)
                                                                .shadow(1.dp, RoundedCornerShape(6.dp))
                                                                .background(c, RoundedCornerShape(6.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("$n", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                            if (nums.size > 4) {
                                                Text("+${nums.size - 4}", fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsScreen(
    s: AppStrings,
    jobs: List<Job>,
    days: Map<String, DayState>,
    year: Int,
    month: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayMonth: () -> Unit,
    onDayClick: (Int, Int, Int) -> Unit
) {
    val monthPrefix = "%04d-%02d-".format(year, month + 1)
    val monthDays = days.filterKeys { it.startsWith(monthPrefix) }
    val tickedDays = monthDays.count { it.value.jobNumbers.isNotEmpty() }
    val totalJobDays = monthDays.values.sumOf { it.jobNumbers.size }
    val unfinished = monthDays.filter { !it.value.done &&
        (it.value.scheduled || it.value.jobNumbers.isNotEmpty()) }
        .toList().sortedBy { it.first }
    val daysInThisMonth = daysInMonth(year, month)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text(s.statsTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(s.statsNote, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        MonthNav(s, year, month, onPrevMonth, onNextMonth, onTodayMonth)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(end = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$tickedDays", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Text(s.daysTicked, fontSize = 13.sp)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(start = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$totalJobDays", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                    Text(s.jobTicks, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(s.perJobInMonth, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        if (jobs.isEmpty()) {
            Text(s.noProjectsStats)
        } else {
            for (job in jobs) {
                val count = monthDays.count { it.value.jobNumbers.contains(job.number) }
                val progress = if (daysInThisMonth > 0) count.toFloat() / daysInThisMonth else 0f
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp)
                                .shadow(3.dp, RoundedCornerShape(12.dp))
                                .background(jobColor(job), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("${job.number}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.jobLabel(job.number, job.name), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            LinearProgressIndicator(progress = { progress }, color = jobColor(job), modifier = Modifier.fillMaxWidth().height(7.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("$count ${s.dayCountSuffix}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(s.unfinishedTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        if (unfinished.isEmpty()) {
            Text(s.noUnfinished)
        } else {
            for ((key, state) in unfinished) {
                val parts = key.split("-")
                val d = parts[2].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 1
                val label = when {
                    state.scheduled && state.jobNumbers.isEmpty() -> s.scheduledNotTicked
                    state.jobNumbers.isNotEmpty() -> "${state.jobNumbers.size} ${s.tickedNotDone}"
                    else -> s.notDone
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${s.unfinishedDayLabel(d, m)}: $label", modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Button(onClick = {
                            val y = parts[0].toIntOrNull() ?: year
                            onDayClick(y, m - 1, d)
                        }) { Text(s.openDay, fontSize = 13.sp) }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun DayDialog(
    s: AppStrings,
    year: Int,
    month: Int,
    day: Int,
    jobs: List<Job>,
    state: DayState,
    onToggleJob: (Int) -> Unit,
    onToggleScheduled: () -> Unit,
    onToggleDone: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState())) {
                Text(s.dayTitle(day, month, year), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (state.done) {
                    Text(s.dayDoneNote, color = Color(0xFF2E7D32), fontSize = 14.sp)
                } else if (state.scheduled) {
                    Text(s.dayScheduledNote, color = Color(0xFFE65100), fontSize = 14.sp)
                } else {
                    Text(s.dayTapHint, fontSize = 14.sp)
                }
                Spacer(Modifier.height(12.dp))
                if (jobs.isEmpty()) {
                    Text(s.noJobsDay, fontSize = 14.sp)
                } else {
                    for (rowJobs in jobs.chunked(3)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (job in rowJobs) {
                                val selected = state.jobNumbers.contains(job.number)
                                Button(
                                    onClick = { onToggleJob(job.number) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) jobColor(job) else Color(0xFFEEEEEE),
                                        contentColor = if (selected) Color.White else Color.Black
                                    ),
                                    modifier = Modifier.weight(1f).padding(4.dp).height(64.dp)
                                        .shadow(if (selected) 5.dp else 2.dp, RoundedCornerShape(16.dp)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${job.number}", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                        Text(job.name, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                            repeat(3 - rowJobs.size) { Spacer(Modifier.weight(1f).padding(4.dp)) }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onToggleScheduled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.scheduled) Color(0xFFEF6C00) else Color(0xFFEEEEEE),
                            contentColor = if (state.scheduled) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) { Text(if (state.scheduled) s.scheduledDone else s.scheduled, fontSize = 14.sp) }
                    Button(
                        onClick = onToggleDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.done) Color(0xFF2E7D32) else Color(0xFFEEEEEE),
                            contentColor = if (state.done) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) { Text(if (state.done) s.doneDone else s.done, fontSize = 14.sp) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(s.close) }
                }
            }
        }
    }
}

@Composable
fun ProjectsScreen(
    s: AppStrings,
    lang: String,
    onLangChange: (String) -> Unit,
    jobs: List<Job>,
    onBack: () -> Unit,
    onSaveJobs: (List<Job>) -> Unit
) {
    var editing by remember { mutableStateOf<Job?>(null) }
    var adding by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(s.projectsBack, fontSize = 13.sp) }
            Text(s.projectsTitle, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Text(s.projectsHint, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Text(s.languageLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onLangChange("vi") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (lang == "vi") Color(0xFF2E7D32) else Color(0xFFEEEEEE),
                    contentColor = if (lang == "vi") Color.White else Color.Black
                ),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) { Text("Tiếng Việt", fontSize = 14.sp) }
            Button(
                onClick = { onLangChange("en") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (lang == "en") Color(0xFF2E7D32) else Color(0xFFEEEEEE),
                    contentColor = if (lang == "en") Color.White else Color.Black
                ),
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) { Text("English", fontSize = 14.sp) }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(16.dp)) { Text(s.addNewJob, fontSize = 15.sp) }
        Spacer(Modifier.height(12.dp))
        for (job in jobs) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .background(jobColor(job), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("${job.number}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(10.dp))
                    Text(job.name, modifier = Modifier.weight(1f), fontSize = 15.sp)
                    TextButton(onClick = { editing = job }) { Text(s.edit, fontSize = 13.sp) }
                    TextButton(onClick = {
                        onSaveJobs(jobs.filter { it.number != job.number })
                    }) { Text(s.delete, fontSize = 13.sp) }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(s.deleteNote, fontSize = 12.sp, color = Color.Gray)
    }
    if (adding || editing != null) {
        val initial = editing
        val nextNumber = (jobs.maxOfOrNull { it.number } ?: 0) + 1
        JobDialog(
            s = s,
            initial = initial,
            defaultNumber = nextNumber,
            existingNumbers = jobs.map { it.number }.toSet(),
            onDismiss = { adding = false; editing = null },
            onSave = { number, name, colorIndex ->
                val newJob = Job(number, name, colorIndex)
                val updated = if (initial != null) {
                    jobs.map { if (it.number == initial.number) newJob else it }
                } else {
                    jobs + newJob
                }
                onSaveJobs(updated.sortedBy { it.number })
                adding = false; editing = null
            }
        )
    }
}

@Composable
fun JobDialog(
    s: AppStrings,
    initial: Job?,
    defaultNumber: Int,
    existingNumbers: Set<Int>,
    onDismiss: () -> Unit,
    onSave: (Int, String, Int) -> Unit
) {
    var numberText by remember { mutableStateOf((initial?.number ?: defaultNumber).toString()) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var colorIndex by remember { mutableStateOf(initial?.colorIndex ?: 0) }
    var error by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(if (initial != null) s.jobDialogEdit else s.jobDialogAdd, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                TextField(
                    value = numberText, onValueChange = { numberText = it.filter { c -> c.isDigit() } },
                    label = { Text(s.numberLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(s.nameLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(s.colorLabel)
                Spacer(Modifier.height(6.dp))
                Row {
                    for (i in JOB_COLORS.indices) {
                        Box(
                            modifier = Modifier.size(40.dp).padding(3.dp)
                                .background(JOB_COLORS[i], RoundedCornerShape(20.dp))
                                .border(
                                    width = if (colorIndex == i) 3.dp else 0.dp,
                                    color = if (colorIndex == i) Color.Black else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { colorIndex = i }
                        )
                    }
                }
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(error, color = Color.Red)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(s.cancel) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val n = numberText.toIntOrNull()
                        when {
                            n == null || n <= 0 -> error = s.errNumberPositive
                            name.isBlank() -> error = s.errNameRequired
                            initial == null && existingNumbers.contains(n) -> error = s.errNumberExists(n)
                            initial != null && initial.number != n && existingNumbers.contains(n) -> error = s.errNumberExists(n)
                            else -> onSave(n, name.trim(), colorIndex)
                        }
                    }) { Text(s.save) }
                }
            }
        }
    }
}
