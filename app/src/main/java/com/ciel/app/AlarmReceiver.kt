package com.ciel.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** Receiver for test alarms from the dev debug panel. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: "Test Alarm"
        Log.d("CielDebug", "Test alarm triggered: $label")
        // Could show a notification here if needed
    }
}