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

    /** Vrai tant que l'utilisateur n'a pas arrêté le serveur : il démarre alors tout seul. */
    var actif: Boolean
        get() = prefs.getBoolean("actif", true)
        set(v) { prefs.edit().putBoolean("actif", v).apply() }

    /** Vrai si Ciel doit respecter le mode « Ne pas déranger » du téléphone. */
    var respecterDnd: Boolean
        get() = prefs.getBoolean("respecterDnd", true)
        set(v) { prefs.edit().putBoolean("respecterDnd", v).apply() }

    fun nouveauJeton(): String {
        val octets = ByteArray(18).also { SecureRandom().nextBytes(it) }
        val j = octets.joinToString("") { "%02x".format(it) }
        prefs.edit().putString("jeton", j).apply()
        return j
    }

    companion object { const val PORT_DEFAUT = 8081 }
}
