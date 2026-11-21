package org.openfocus.app.planner

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PlannerTask(val id: String, val title: String, val completed: Boolean)

class PlannerStore(context: Context) {
    private val preferences = context.getSharedPreferences("planner_tasks", Context.MODE_PRIVATE)

    fun load(): List<PlannerTask> {
        val raw = preferences.getString(KEY_TASKS, "[]") ?: "[]"
        return runCatching {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val title = item.optString("title").trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                PlannerTask(id, title, item.optBoolean("completed", false))
            }
        }.getOrDefault(emptyList())
    }

    fun add(title: String): PlannerTask {
        val cleanTitle = title.trim().take(MAX_TITLE_LENGTH)
        require(cleanTitle.isNotEmpty()) { "Task title cannot be empty." }
        val task = PlannerTask(UUID.randomUUID().toString(), cleanTitle, false)
        persist(load() + task)
        return task
    }

    fun setCompleted(id: String, completed: Boolean) {
        persist(load().map { if (it.id == id) it.copy(completed = completed) else it })
    }

    fun delete(id: String) {
        persist(load().filterNot { it.id == id })
    }

    private fun persist(tasks: List<PlannerTask>) {
        val json = JSONArray()
        tasks.forEach { task ->
            json.put(JSONObject().put("id", task.id).put("title", task.title).put("completed", task.completed))
        }
        preferences.edit().putString(KEY_TASKS, json.toString()).apply()
    }

    companion object {
        private const val KEY_TASKS = "tasks"
        private const val MAX_TITLE_LENGTH = 180
    }
}