package vn.phong.socongthang.data

import android.content.Context
import android.content.SharedPreferences

data class Job(val number: Int, val name: String, val colorIndex: Int)

data class DayState(
    val jobNumbers: Set<Int> = emptySet(),
    val done: Boolean = false,
    val scheduled: Boolean = false
)

fun dateKey(year: Int, month: Int, day: Int): String {
    // month is 0-based (Calendar), key is yyyy-MM-dd
    return "%04d-%02d-%02d".format(year, month + 1, day)
}

class Store(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("so_cong_thang", Context.MODE_PRIVATE)

    fun loadLang(): String? = prefs.getString("lang", null)

    fun saveLang(lang: String) {
        prefs.edit().putString("lang", lang).apply()
    }

    fun loadJobs(lang: String = "vi"): List<Job> {
        val raw = prefs.getString("jobs", null)
        if (raw.isNullOrBlank()) {
            val prefix = if (lang == "en") "Job" else "Việc"
            return listOf(
                Job(1, "$prefix 1", 0),
                Job(2, "$prefix 2", 1),
                Job(3, "$prefix 3", 2)
            )
        }
        val out = mutableListOf<Job>()
        for (line in raw.lines()) {
            if (line.isBlank()) continue
            val parts = line.split("|")
            if (parts.size < 3) continue
            val n = parts[0].toIntOrNull() ?: continue
            val color = parts[2].toIntOrNull() ?: 0
            out.add(Job(n, parts[1], color))
        }
        return out.sortedBy { it.number }.ifEmpty {
            val prefix = if (lang == "en") "Job" else "Việc"
            listOf(Job(1, "$prefix 1", 0))
        }
    }

    fun saveJobs(jobs: List<Job>) {
        val text = jobs.sortedBy { it.number }.joinToString("\n") { job ->
            val safeName = job.name.replace("|", " ").replace("\n", " ").trim()
            "${job.number}|$safeName|${job.colorIndex}"
        }
        prefs.edit().putString("jobs", text).apply()
    }

    fun loadDays(): Map<String, DayState> {
        val raw = prefs.getString("days", null) ?: return emptyMap()
        val out = mutableMapOf<String, DayState>()
        for (line in raw.lines()) {
            if (line.isBlank()) continue
            val parts = line.split("|")
            if (parts.size < 4) continue
            val jobs = if (parts[1].isBlank()) emptySet()
            else parts[1].split(",").mapNotNull { it.toIntOrNull() }.toSet()
            out[parts[0]] = DayState(
                jobNumbers = jobs,
                done = parts[2] == "1",
                scheduled = parts[3] == "1"
            )
        }
        return out
    }

    fun saveDays(days: Map<String, DayState>) {
        val text = days.entries.sortedBy { it.key }.joinToString("\n") { (key, state) ->
            val jobs = state.jobNumbers.sorted().joinToString(",")
            val done = if (state.done) "1" else "0"
            val sch = if (state.scheduled) "1" else "0"
            "$key|$jobs|$done|$sch"
        }
        prefs.edit().putString("days", text).apply()
    }
}
