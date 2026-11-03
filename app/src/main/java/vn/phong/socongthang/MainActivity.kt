package vn.phong.socongthang

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import java.util.Calendar
import vn.phong.socongthang.data.DayState
import vn.phong.socongthang.data.Job
import vn.phong.socongthang.data.Store
import vn.phong.socongthang.data.dateKey
import vn.phong.socongthang.data.scheduleReminder
import vn.phong.socongthang.ui.CalendarScreen
import vn.phong.socongthang.ui.DayDialog
import vn.phong.socongthang.ui.ProjectsScreen

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
                var jobs by remember { mutableStateOf(store.loadJobs()) }
                var days by remember { mutableStateOf(store.loadDays()) }
                var showProjects by remember { mutableStateOf(false) }
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

                if (showProjects) {
                    ProjectsScreen(
                        jobs = jobs,
                        onBack = { showProjects = false },
                        onSaveJobs = { saveJobs(it) }
                    )
                } else {
                    CalendarScreen(
                        jobs = jobs,
                        days = days,
                        year = viewYear,
                        month = viewMonth,
                        onPrevMonth = {
                            if (viewMonth == 0) { viewMonth = 11; viewYear -= 1 } else viewMonth -= 1
                        },
                        onNextMonth = {
                            if (viewMonth == 11) { viewMonth = 0; viewYear += 1 } else viewMonth += 1
                        },
                        onTodayMonth = {
                            val c = Calendar.getInstance()
                            viewYear = c.get(Calendar.YEAR)
                            viewMonth = c.get(Calendar.MONTH)
                        },
                        onDayClick = { y, m, d ->
                            viewYear = y
                            viewMonth = m
                            selectedDay = Triple(y, m, d)
                        },
                        onOpenProjects = { showProjects = true }
                    )
                }

                val sel = selectedDay
                if (sel != null) {
                    val (y, m, d) = sel
                    val key = dateKey(y, m, d)
                    val state = days[key] ?: DayState()
                    DayDialog(
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
