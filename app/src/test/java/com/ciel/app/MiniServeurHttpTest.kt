package com.ciel.app

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

/** Démarre le vrai serveur sur un port libre et l'interroge en HTTP. */
class MiniServeurHttpTest {
    private val jeton = "secret42"
    private lateinit var serveur: MiniServeurHttp
    private val recues = mutableListOf<Commande>()

    @Before fun demarrer() {
        serveur = MiniServeurHttp(0) { req ->
            when (val r = CielCommandProcessor.traiter(req.chemin, req.params, req.entetes["authorization"], jeton)) {
                is Resultat.Refuse -> Reponse(401, MiniServeurHttp.json(false, "refusé"))
                is Resultat.Erreur -> Reponse(r.code, MiniServeurHttp.json(false, r.message))
                is Resultat.Ok -> { recues += r.commande; Reponse(200, MiniServeurHttp.json(true, "ok")) }
            }
        }
        serveur.demarrer()
    }

    @After fun arreter() = serveur.arreter()

    private fun get(chemin: String, auth: String? = null): Pair<Int, String> {
        val c = URL("http://127.0.0.1:${serveur.portEcoute}$chemin").openConnection() as HttpURLConnection
        if (auth != null) c.setRequestProperty("Authorization", auth)
        val code = c.responseCode
        val corps = (if (code < 400) c.inputStream else c.errorStream).bufferedReader().readText()
        return code to corps
    }

    @Test fun status() {
        val (code, corps) = get("/status")
        assertEquals(200, code)
        assertTrue(corps.contains("\"ok\":true"))
    }

    @Test fun minuteurAvecEnTete() {
        assertEquals(200, get("/minuteur?secondes=45&message=Th%C3%A9", "Bearer $jeton").first)
        assertEquals(listOf<Commande>(Commande.Minuteur(45, "Thé")), recues)
    }

    @Test fun alarmeAvecTokenEnParametre() {
        assertEquals(200, get("/ALARME?HEURE=6&MINUTE=5&token=$jeton").first)
        assertEquals(listOf<Commande>(Commande.Alarme(6, 5, "")), recues)
    }

    @Test fun refusSansJeton() {
        assertEquals(401, get("/minuteur?secondes=45").first)
        assertTrue(recues.isEmpty())
    }

    @Test fun erreurs() {
        assertEquals(400, get("/minuteur?secondes=0&token=$jeton").first)
        assertEquals(404, get("/inconnu").first)
    }

    @Test fun jsonEchappe() {
        assertEquals("{\"ok\":false,\"message\":\"a \\\"b\\\" c\\\\d\"}", MiniServeurHttp.json(false, "a \"b\" c\\d"))
    }
}
