package com.ciel.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Relance le serveur après un redémarrage du téléphone s'il était actif avant. */
class DemarrageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && Reglages(context).actif) {
            CommandServerService.demarrer(context)
        }
    }
}
