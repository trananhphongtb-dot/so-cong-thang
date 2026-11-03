package vn.phong.socongthang.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
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
private fun MonthNav(
    year: Int,
    month: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayMonth: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onPrevMonth, modifier = Modifier.size(52.dp, 48.dp)) { Text("<", fontSize = 20.sp) }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tháng ${month + 1} / $year",
                    fontSize = 21.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onNextMonth, modifier = Modifier.size(52.dp, 48.dp)) { Text(">", fontSize = 20.sp) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = onTodayMonth) { Text("Về hôm nay") }
            }
        }
    }
}

@Composable
fun CalendarScreen(
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

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        // Header with depth: green gradient, rounded, shadow
        Box(
            modifier = Modifier.fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF66BB6A), Color(0xFF2E7D32), Color(0xFF1B5E20))
                    ),
                    RoundedCornerShape(26.dp)
                )
                .padding(22.dp)
        ) {
            Column {
                Text("Sổ Công Tháng", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(6.dp))
                Text("Chạm vào 1 ngày để tích việc, thoáng và rõ từng ngày.", fontSize = 14.sp, color = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
        if (showTodayBanner) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    onDayClick(
                        todayCal.get(Calendar.YEAR),
                        todayCal.get(Calendar.MONTH),
                        todayCal.get(Calendar.DAY_OF_MONTH)
                    )
                }
            ) {
                Text(
                    "Hôm nay chưa bấm Đã xong. Bấm vào đây để chốt ngày.",
                    modifier = Modifier.padding(16.dp), color = Color(0xFFE65100), fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        MonthNav(year, month, onPrevMonth, onNextMonth, onTodayMonth)
        Spacer(Modifier.height(16.dp))

        // Calendar card: white, raised, lots of breathing room
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (w in weekdays) {
                        Text(w, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(10.dp))
                val total = daysInMonth(year, month)
                val cells = mutableListOf<Int?>()
                repeat(firstOffset(year, month)) { cells.add(null) }
                for (d in 1..total) cells.add(d)
                while (cells.size % 7 != 0) cells.add(null)
                for (week in cells.chunked(7)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (day in week) {
                            if (day == null) {
                                Box(modifier = Modifier.weight(1f).height(92.dp))
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
                                    modifier = Modifier.weight(1f).height(92.dp).padding(3.dp)
                                        .shadow(4.dp, RoundedCornerShape(18.dp))
                                        .background(bg, RoundedCornerShape(18.dp))
                                        .border(
                                            width = if (isToday) 2.dp else 0.dp,
                                            color = if (isToday) Color(0xFF2E7D32) else Color.Transparent,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .clickable { onDayClick(year, month, day) }
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$day", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            if (state?.done == true) {
                                                Text(" ✓", fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                            } else if (state?.scheduled == true) {
                                                Text(" •Hẹn", fontSize = 10.sp, color = Color(0xFFE65100))
                                            }
                                        }
                                        if (state != null && state.jobNumbers.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            val nums = state.jobNumbers.sorted()
                                            for (rowNums in nums.chunked(2)) {
                                                Row {
                                                    for (n in rowNums) {
                                                        val job = jobByNumber[n]
                                                        val c = if (job != null) jobColor(job) else Color.Gray
                                                        Box(
                                                            modifier = Modifier.size(24.dp).padding(1.dp)
                                                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                                                .background(c, RoundedCornerShape(8.dp)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("$n", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                            if (nums.size > 4) {
                                                Text("+${nums.size - 4}", fontSize = 10.sp, color = Color.Gray)
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
        Spacer(Modifier.height(12.dp))
        Text(
            "Dữ liệu lưu trên máy, không cần mạng. Nhắc lúc 20h30 nếu ngày có hẹn hoặc đã tích mà chưa bấm Đã xong và máy đã cho phép thông báo.",
            fontSize = 12.sp, color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun StatsScreen(
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
        Text("Thống kê", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Nhìn 1 lần là biết tháng này làm được bao nhiêu.", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        MonthNav(year, month, onPrevMonth, onNextMonth, onTodayMonth)
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f).padding(end = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("$tickedDays", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    Text("ngày đã tích", fontSize = 14.sp)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f).padding(start = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("$totalJobDays", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                    Text("lượt việc đã tích", fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Từng việc trong tháng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (jobs.isEmpty()) {
            Text("Chưa có dự án nào. Sang tab Dự án để thêm việc theo số.")
        } else {
            for (job in jobs) {
                val count = monthDays.count { it.value.jobNumbers.contains(job.number) }
                val progress = if (daysInThisMonth > 0) count.toFloat() / daysInThisMonth else 0f
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp))
                                .background(jobColor(job), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("${job.number}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Số ${job.number} - ${job.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(progress = { progress }, color = jobColor(job), modifier = Modifier.fillMaxWidth().height(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("$count ngày", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Chưa tích / chưa xong trong tháng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (unfinished.isEmpty()) {
            Text("Không có ngày nào chưa xong trong tháng này.")
        } else {
            for ((key, state) in unfinished) {
                val parts = key.split("-")
                val d = parts[2].toIntOrNull() ?: 0
                val m = parts[1].toIntOrNull() ?: 1
                val label = when {
                    state.scheduled && state.jobNumbers.isEmpty() -> "Có hẹn, chưa tích"
                    state.jobNumbers.isNotEmpty() -> "Đã tích ${state.jobNumbers.size} việc, chưa bấm Đã xong"
                    else -> "Chưa xong"
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ngày $d/$m: $label", modifier = Modifier.weight(1f))
                        Button(onClick = {
                            val y = parts[0].toIntOrNull() ?: year
                            onDayClick(y, m - 1, d)
                        }) { Text("Mở ngày") }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun DayDialog(
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
        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text("Ngày $day/${month + 1}/$year", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                if (state.done) {
                    Text("Ngày này đã xong, sẽ không nhắc nữa.", color = Color(0xFF2E7D32))
                } else if (state.scheduled) {
                    Text("Ngày này đã hẹn trước.", color = Color(0xFFE65100))
                } else {
                    Text("Bấm vào số để tích / bỏ tích việc cho ngày này.")
                }
                Spacer(Modifier.height(14.dp))
                if (jobs.isEmpty()) {
                    Text("Chưa có việc nào. Đóng bảng này rồi sang tab Dự án để thêm việc theo số.")
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
                                    modifier = Modifier.weight(1f).padding(4.dp).height(72.dp)
                                        .shadow(if (selected) 6.dp else 2.dp, RoundedCornerShape(18.dp)),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${job.number}", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                                        Text(job.name, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                            repeat(3 - rowJobs.size) { Spacer(Modifier.weight(1f).padding(4.dp)) }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onToggleScheduled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.scheduled) Color(0xFFEF6C00) else Color(0xFFEEEEEE),
                            contentColor = if (state.scheduled) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) { Text(if (state.scheduled) "Đã hẹn ✓" else "Hẹn trước") }
                    Button(
                        onClick = onToggleDone,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.done) Color(0xFF2E7D32) else Color(0xFFEEEEEE),
                            contentColor = if (state.done) Color.White else Color.Black
                        ),
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) { Text(if (state.done) "Đã xong ✓" else "Đã xong") }
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Đóng") }
                }
            }
        }
    }
}

@Composable
fun ProjectsScreen(
    jobs: List<Job>,
    onBack: () -> Unit,
    onSaveJobs: (List<Job>) -> Unit
) {
    var editing by remember { mutableStateOf<Job?>(null) }
    var adding by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("< Quay lại") }
            Text("Dự án việc theo số", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Text("Mỗi việc gắn 1 số. Tích số nào thì số đó hiện lên ô ngày.")
        Spacer(Modifier.height(14.dp))
        Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("+ Thêm việc mới", fontSize = 16.sp) }
        Spacer(Modifier.height(14.dp))
        for (job in jobs) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .background(jobColor(job), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("${job.number}", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Text(job.name, modifier = Modifier.weight(1f), fontSize = 16.sp)
                    TextButton(onClick = { editing = job }) { Text("Sửa") }
                    TextButton(onClick = {
                        onSaveJobs(jobs.filter { it.number != job.number })
                    }) { Text("Xóa") }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Xóa việc chỉ xóa tên việc, các số đã tích trước đó vẫn còn hiện trên lịch.", fontSize = 12.sp, color = Color.Gray)
    }
    if (adding || editing != null) {
        val initial = editing
        val nextNumber = (jobs.maxOfOrNull { it.number } ?: 0) + 1
        JobDialog(
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
        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(if (initial != null) "Sửa việc" else "Thêm việc mới", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                TextField(
                    value = numberText, onValueChange = { numberText = it.filter { c -> c.isDigit() } },
                    label = { Text("Số việc") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Tên việc") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                Text("Chọn màu")
                Spacer(Modifier.height(8.dp))
                Row {
                    for (i in JOB_COLORS.indices) {
                        Box(
                            modifier = Modifier.size(44.dp).padding(3.dp)
                                .background(JOB_COLORS[i], RoundedCornerShape(22.dp))
                                .border(
                                    width = if (colorIndex == i) 3.dp else 0.dp,
                                    color = if (colorIndex == i) Color.Black else Color.Transparent,
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable { colorIndex = i }
                        )
                    }
                }
                if (error.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Color.Red)
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val n = numberText.toIntOrNull()
                        when {
                            n == null || n <= 0 -> error = "Nhập số việc lớn hơn 0."
                            name.isBlank() -> error = "Nhập tên việc."
                            initial == null && existingNumbers.contains(n) -> error = "Số $n đã có rồi, chọn số khác."
                            initial != null && initial.number != n && existingNumbers.contains(n) -> error = "Số $n đã có rồi, chọn số khác."
                            else -> onSave(n, name.trim(), colorIndex)
                        }
                    }) { Text("Lưu") }
                }
            }
        }
    }
}
