package com.ciel.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class LogManager(private val logFile: File) {
    fun logAction(action: String, details: String) {
        val logs = loadLogs()
        val entry = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("action", action)
            put("details", details)
        }
        logs.put(entry)
        
        // Keep only last 50
        val trimmed = JSONArray()
        val start = maxOf(0, logs.length() - 50)
        for (i in start until logs.length()) {
            trimmed.put(logs.get(i))
        }
        
        try {
            logFile.writeText(trimmed.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogsJson(): String {
        return if (logFile.exists()) {
            try {
                logFile.readText()
            } catch (e: Exception) {
                "[]"
            }
        } else {
            "[]"
        }
    }

    private fun loadLogs(): JSONArray {
        if (!logFile.exists()) return JSONArray()
        return try {
            JSONArray(logFile.readText())
        } catch (e: Exception) {
            JSONArray()
        }
    }
}
