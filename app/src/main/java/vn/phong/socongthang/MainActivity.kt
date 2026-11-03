package vn.phong.socongthang

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Calendar
import vn.phong.socongthang.data.DayState
import vn.phong.socongthang.data.Job
import vn.phong.socongthang.data.Store
import vn.phong.socongthang.data.dateKey
import vn.phong.socongthang.data.scheduleReminder
import vn.phong.socongthang.ui.CalendarScreen
import vn.phong.socongthang.ui.DayDialog
import vn.phong.socongthang.ui.LanguageScreen
import vn.phong.socongthang.ui.ProjectsScreen
import vn.phong.socongthang.ui.StatsScreen
import vn.phong.socongthang.ui.stringsFor

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        try {
            scheduleReminder(this)
        } catch (_: Exception) {
        }
        setContent {
            MaterialTheme {
                var lang by remember { mutableStateOf(store.loadLang()) }
                if (lang == null) {
                    LanguageScreen(onChoose = { chosen ->
                        store.saveLang(chosen)
                        lang = chosen
                    })
                    return@MaterialTheme
                }
                val currentLang = lang ?: "vi"
                val s = stringsFor(currentLang)
                var jobs by remember { mutableStateOf(store.loadJobs(currentLang)) }
                var days by remember { mutableStateOf(store.loadDays()) }
                var tab by remember { mutableStateOf(0) }
                val now = Calendar.getInstance()
                var viewYear by remember { mutableStateOf(now.get(Calendar.YEAR)) }
                var viewMonth by remember { mutableStateOf(now.get(Calendar.MONTH)) }
                var selectedDay by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }

                fun saveJobs(next: List<Job>) {
                    jobs = next
                    store.saveJobs(next)
                }
                fun saveDays(next: Map<String, DayState>) {
                    days = next
                    store.saveDays(next)
                }
                fun updateDay(key: String, transform: (DayState) -> DayState) {
                    val current = days[key] ?: DayState()
                    val updated = transform(current)
                    val next = days.toMutableMap()
                    if (updated.jobNumbers.isEmpty() && !updated.done && !updated.scheduled) {
                        next.remove(key)
                    } else {
                        next[key] = updated
                    }
                    saveDays(next)
                }
                fun prevMonth() {
                    if (viewMonth == 0) { viewMonth = 11; viewYear -= 1 } else viewMonth -= 1
                }
                fun nextMonth() {
                    if (viewMonth == 11) { viewMonth = 0; viewYear += 1 } else viewMonth += 1
                }
                fun todayMonth() {
                    val c = Calendar.getInstance()
                    viewYear = c.get(Calendar.YEAR)
                    viewMonth = c.get(Calendar.MONTH)
                }
                fun openDay(y: Int, m: Int, d: Int) {
                    viewYear = y
                    viewMonth = m
                    selectedDay = Triple(y, m, d)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (tab) {
                            0 -> CalendarScreen(
                                s = s,
                                jobs = jobs,
                                days = days,
                                year = viewYear,
                                month = viewMonth,
                                onPrevMonth = { prevMonth() },
                                onNextMonth = { nextMonth() },
                                onTodayMonth = { todayMonth() },
                                onDayClick = { y, m, d -> openDay(y, m, d) }
                            )
                            1 -> StatsScreen(
                                s = s,
                                jobs = jobs,
                                days = days,
                                year = viewYear,
                                month = viewMonth,
                                onPrevMonth = { prevMonth() },
                                onNextMonth = { nextMonth() },
                                onTodayMonth = { todayMonth() },
                                onDayClick = { y, m, d -> tab = 0; openDay(y, m, d) }
                            )
                            else -> ProjectsScreen(
                                s = s,
                                lang = currentLang,
                                onLangChange = { chosen ->
                                    store.saveLang(chosen)
                                    lang = chosen
                                },
                                jobs = jobs,
                                onBack = { tab = 0 },
                                onSaveJobs = { saveJobs(it) }
                            )
                        }
                    }
                    NavigationBar(modifier = Modifier.height(58.dp)) {
                        NavigationBarItem(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            icon = { Text("📅", fontSize = 14.sp) },
                            label = { Text(s.calendarTab, fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            icon = { Text("📊", fontSize = 14.sp) },
                            label = { Text(s.statsTab, fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = tab == 2,
                            onClick = { tab = 2 },
                            icon = { Text("🗂️", fontSize = 14.sp) },
                            label = { Text(s.projectsTab, fontSize = 10.sp) }
                        )
                    }
                }

                val sel = selectedDay
                if (sel != null) {
                    val (y, m, d) = sel
                    val key = dateKey(y, m, d)
                    val state = days[key] ?: DayState()
                    DayDialog(
                        s = s,
                        year = y, month = m, day = d, jobs = jobs, state = state,
                        onToggleJob = { number ->
                            updateDay(key) { cur ->
                                val nums = cur.jobNumbers.toMutableSet()
                                if (nums.contains(number)) nums.remove(number) else nums.add(number)
                                cur.copy(jobNumbers = nums)
                            }
                        },
                        onToggleScheduled = {
                            updateDay(key) { cur -> cur.copy(scheduled = !cur.scheduled) }
                        },
                        onToggleDone = {
                            updateDay(key) { cur -> cur.copy(done = !cur.done) }
                        },
                        onDismiss = { selectedDay = null }
                    )
                }
            }
        }
    }
}
