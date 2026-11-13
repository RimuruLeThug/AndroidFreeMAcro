package com.ciel.app

import android.content.Context
import android.os.PowerManager

class BatteryManager(private val context: Context) {
    fun isIgnoringOptimizations(): Boolean {
        return context.getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }
}
