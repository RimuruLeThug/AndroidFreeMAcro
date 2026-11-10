package com.ciel.app

import android.content.Context
import java.security.SecureRandom

/** Réglages persistants : jeton d'accès, port, et « serveur à relancer au démarrage ». */
class Reglages(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ciel", Context.MODE_PRIVATE)

    val port: Int get() = prefs.getInt("port", PORT_DEFAUT)

    /** Jeton aléatoire généré au premier lancement (jamais dans le code source). */
    val jeton: String
        get() = prefs.getString("jeton", null) ?: nouveauJeton()

    var actif: Boolean
        get() = prefs.getBoolean("actif", false)
        set(v) { prefs.edit().putBoolean("actif", v).apply() }

    fun nouveauJeton(): String {
        val octets = ByteArray(18).also { SecureRandom().nextBytes(it) }
        val j = octets.joinToString("") { "%02x".format(it) }
        prefs.edit().putString("jeton", j).apply()
        return j
    }

    companion object { const val PORT_DEFAUT = 8081 }
}
