package com.ciel.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CielCommandProcessorTest {
    private val jeton = "abc123"

    private fun traiter(chemin: String, params: Map<String, String> = emptyMap(), auth: String? = "Bearer $jeton") =
        CielCommandProcessor.traiter(chemin, params, auth, jeton)

    @Test fun statutSansJeton() = assertEquals(Resultat.Ok(Commande.Statut), traiter("/status", auth = null))

    @Test fun minuteurRefuseSansJeton() = assertEquals(Resultat.Refuse, traiter("/minuteur", mapOf("secondes" to "60"), null))

    @Test fun minuteurRefuseMauvaisJeton() =
        assertEquals(Resultat.Refuse, traiter("/minuteur", mapOf("secondes" to "60"), "Bearer faux"))

    @Test fun jetonVideRefuseTout() =
        assertEquals(Resultat.Refuse, CielCommandProcessor.traiter("/minuteur", mapOf("secondes" to "60"), "Bearer ", ""))

    @Test fun jetonEnParametre() =
        assertEquals(Resultat.Ok(Commande.Minuteur(60, "")), traiter("/minuteur", mapOf("secondes" to "60", "token" to jeton), null))

    @Test fun minuteurFormatMacroDroid() =
        assertEquals(Resultat.Ok(Commande.Minuteur(90, "Pâtes")), traiter("/MINUTEUR", mapOf("SECONDES" to "90", "MESSAGE" to "Pâtes")))

    @Test fun minuteurAdditionneLesUnites() =
        assertEquals(
            Resultat.Ok(Commande.Minuteur(3600 + 120 + 5, "")),
            traiter("/minuteur", mapOf("heures" to "1", "minutes" to "2", "secondes" to "5"))
        )

    @Test fun minuteurBornes() {
        assertTrue(traiter("/minuteur", mapOf("secondes" to "0")) is Resultat.Erreur)
        assertTrue(traiter("/minuteur", mapOf("secondes" to "86401")) is Resultat.Erreur)
        assertTrue(traiter("/minuteur", mapOf("secondes" to "abc")) is Resultat.Erreur)
        assertEquals(Resultat.Ok(Commande.Minuteur(86400, "")), traiter("/minuteur", mapOf("heures" to "24")))
    }

    @Test fun nappe() = assertEquals(
        Resultat.Ok(Commande.Nappe(1800, "Sieste")),
        traiter("/nappe", mapOf("minutes" to "30"))
    )

    @Test fun alarme() = assertEquals(
        Resultat.Ok(Commande.Alarme(7, 30, "Réveil")),
        traiter("/alarme", mapOf("HEURE" to "7", "MINUTE" to "30", "libelle" to "Réveil"))
    )

    @Test fun alarmeInvalide() {
        assertTrue(traiter("/alarme", mapOf("heure" to "24")) is Resultat.Erreur)
        assertTrue(traiter("/alarme", mapOf("heure" to "7", "minute" to "60")) is Resultat.Erreur)
        assertTrue(traiter("/alarme", emptyMap()) is Resultat.Erreur)
    }

    @Test fun routeInconnue() {
        val r = traiter("/sms")
        assertTrue(r is Resultat.Erreur && r.code == 404)
    }

    @Test fun autorisation() {
        assertTrue(CielCommandProcessor.autorise("Bearer abc123", null, jeton))
        assertTrue(CielCommandProcessor.autorise("abc123", null, jeton))
        assertFalse(CielCommandProcessor.autorise(null, null, jeton))
        assertFalse(CielCommandProcessor.autorise("Bearer abc1234", null, jeton))
    }
}
