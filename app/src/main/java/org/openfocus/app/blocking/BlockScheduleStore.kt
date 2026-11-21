package org.openfocus.app.blocking

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

data class AppBlockSchedule(
    val id: String,
    val name: String,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val daysOfWeek: Set<Int>,
    val enabled: Boolean
) {
    init {
        require(name.isNotBlank())
        require(startMinuteOfDay in 0..1439)
        require(endMinuteOfDay in 0..1439)
        require(startMinuteOfDay != endMinuteOfDay)
        require(daysOfWeek.all { it in Calendar.SUNDAY..Calendar.SATURDAY })
    }
}

class BlockScheduleStore(context: Context) {
    private val preferences = context.getSharedPreferences("app_block_schedules", Context.MODE_PRIVATE)

    fun load(): List<AppBlockSchedule> {
        val raw = preferences.getString(KEY_SCHEDULES, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val daysJson = item.optJSONArray("days") ?: JSONArray()
                val days = (0 until daysJson.length()).mapNotNull { daysJson.optInt(it).takeIf { day -> day in Calendar.SUNDAY..Calendar.SATURDAY } }.toSet()
                val name = item.optString("name").trim()
                val start = item.optInt("start", -1)
                val end = item.optInt("end", -1)
                val id = item.optString("id")
                runCatching {
                    AppBlockSchedule(
                        id = id.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString(),
                        name = name,
                        startMinuteOfDay = start,
                        endMinuteOfDay = end,
                        daysOfWeek = days,
                        enabled = item.optBoolean("enabled", true)
                    )
                }.getOrNull()
            }
        }.getOrDefault(emptyList())
    }

    fun add(name: String, startMinuteOfDay: Int, endMinuteOfDay: Int, daysOfWeek: Set<Int>): AppBlockSchedule {
        val cleanName = name.trim().take(MAX_NAME_LENGTH)
        require(cleanName.isNotEmpty()) { "Schedule name cannot be empty." }
        val schedule = AppBlockSchedule(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            startMinuteOfDay = startMinuteOfDay,
            endMinuteOfDay = endMinuteOfDay,
            daysOfWeek = daysOfWeek,
            enabled = true
        )
        persist(load() + schedule)
        return schedule
    }

    fun setEnabled(id: String, enabled: Boolean) {
        persist(load().map { if (it.id == id) it.copy(enabled = enabled) else it })
    }

    fun delete(id: String) {
        persist(load().filterNot { it.id == id })
    }

    fun isBlockingNow(nowEpochMillis: Long = System.currentTimeMillis()): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = nowEpochMillis }
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val previousDay = if (day == Calendar.SUNDAY) Calendar.SATURDAY else day - 1
        return load().any { schedule ->
            schedule.enabled && BlockScheduleEvaluator.isActive(
                schedule.startMinuteOfDay,
                schedule.endMinuteOfDay,
                schedule.daysOfWeek,
                day,
                previousDay,
                minute
            )
        }
    }

    private fun persist(schedules: List<AppBlockSchedule>) {
        val array = JSONArray()
        schedules.forEach { schedule ->
            val days = JSONArray()
            schedule.daysOfWeek.sorted().forEach(days::put)
            array.put(
                JSONObject()
                    .put("id", schedule.id)
                    .put("name", schedule.name)
                    .put("start", schedule.startMinuteOfDay)
                    .put("end", schedule.endMinuteOfDay)
                    .put("days", days)
                    .put("enabled", schedule.enabled)
            )
        }
        preferences.edit().putString(KEY_SCHEDULES, array.toString()).apply()
    }

    companion object {
        private const val KEY_SCHEDULES = "schedules"
        private const val MAX_NAME_LENGTH = 64
    }
}

internal object BlockScheduleEvaluator {
    fun isActive(
        startMinute: Int,
        endMinute: Int,
        days: Set<Int>,
        currentDay: Int,
        previousDay: Int,
        currentMinute: Int
    ): Boolean {
        if (days.isEmpty() || currentMinute !in 0..1439 || startMinute == endMinute) return false
        return if (startMinute < endMinute) {
            currentDay in days && currentMinute >= startMinute && currentMinute < endMinute
        } else {
            (currentDay in days && currentMinute >= startMinute) ||
                (previousDay in days && currentMinute < endMinute)
        }
    }
}