package com.ciel.app

import java.security.MessageDigest

/** Commande comprise par l'appli (indépendante d'Android : testable en JVM pur). */
sealed class Commande {
    data class Minuteur(val secondes: Int, val message: String) : Commande()
    data class Alarme(val heure: Int, val minute: Int, val message: String) : Commande()
    object Meteo : Commande()
    object Statut : Commande()
}

sealed class Resultat {
    object Refuse : Resultat()
    data class Erreur(val code: Int, val message: String) : Resultat()
    data class Ok(val commande: Commande) : Resultat()
}

/**
 * Traduit une requête HTTP (chemin + paramètres + en-tête Authorization) en commande.
 *
 * Routes (insensibles à la casse, compatibles avec les anciennes macros MacroDroid) :
 *  - GET /status                                   -> état (sans jeton)
 *  - GET /minuteur?secondes=90[&minutes=..][&heures=..][&message=..]
 *  - GET /alarme?heure=7&minute=30[&message=..]
 * Jeton : en-tête `Authorization: Bearer <jeton>` ou paramètre `token=<jeton>`.
 */
object CielCommandProcessor {

    fun traiter(chemin: String, params: Map<String, String>, auth: String?, jeton: String): Resultat {
        val route = chemin.trim('/').lowercase()
        val p = params.mapKeys { it.key.lowercase() }

        if (route == "status" || route == "statut") return Resultat.Ok(Commande.Statut)
        if (route == "meteo") return Resultat.Ok(Commande.Meteo)
        if (route != "minuteur" && route != "alarme") {
            return Resultat.Erreur(404, "Route inconnue : /$route (routes : /status, /minuteur, /alarme, /meteo)")
        }
        if (!autorise(auth, p["token"], jeton)) return Resultat.Refuse

        val message = (p["message"] ?: p["libelle"] ?: "").take(200)
        return when (route) {
            "minuteur" -> {
                val s = entier(p["secondes"]) ?: 0
                val m = entier(p["minutes"]) ?: 0
                val h = entier(p["heures"]) ?: 0
                val total = s.toLong() + m * 60L + h * 3600L
                if (total !in 1..86400) Resultat.Erreur(400, "Durée entre 1 seconde et 24 heures (reçu : $total s).")
                else Resultat.Ok(Commande.Minuteur(total.toInt(), message))
            }
            else -> {
                val h = entier(p["heure"])
                val m = entier(p["minute"]) ?: 0
                if (h == null || h !in 0..23 || m !in 0..59) Resultat.Erreur(400, "Heure invalide (heure 0-23, minute 0-59).")
                else Resultat.Ok(Commande.Alarme(h, m, message))
            }
        }
    }

    fun autorise(auth: String?, tokenParam: String?, jeton: String): Boolean {
        if (jeton.isBlank()) return false
        val fourni = auth?.trim()?.removePrefix("Bearer")?.trim()?.takeIf { it.isNotEmpty() }
            ?: tokenParam ?: return false
        return MessageDigest.isEqual(fourni.toByteArray(), jeton.toByteArray())
    }

    private fun entier(v: String?): Int? = v?.trim()?.toIntOrNull()
}
