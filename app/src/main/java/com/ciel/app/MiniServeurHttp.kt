package com.ciel.app

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.Executors

data class Requete(val methode: String, val chemin: String, val params: Map<String, String>, val entetes: Map<String, String>)
data class Reponse(val code: Int, val json: String)

/** Serveur HTTP minimal (GET/POST sans corps, réponses JSON), sans dépendance externe. */
class MiniServeurHttp(private val port: Int, private val gestionnaire: (Requete) -> Reponse) {
    private var socket: ServerSocket? = null
    private val pool = Executors.newFixedThreadPool(4)

    /** Port réellement écouté (utile si port = 0). */
    val portEcoute: Int get() = socket?.localPort ?: -1
    val actif: Boolean get() = socket?.isClosed == false

    fun demarrer() {
        val s = ServerSocket()
        s.reuseAddress = true
        s.bind(InetSocketAddress(port))
        socket = s
        Thread({
            while (!s.isClosed) {
                val client = try { s.accept() } catch (e: Exception) { break }
                pool.execute { servir(client) }
            }
        }, "ciel-http").apply { isDaemon = true }.start()
    }

    fun arreter() {
        try { socket?.close() } catch (_: Exception) {}
        pool.shutdownNow()
    }

    private fun servir(client: Socket) {
        client.use { c ->
            c.soTimeout = 5000
            val reponse = try {
                val lecteur = BufferedReader(InputStreamReader(c.getInputStream(), Charsets.UTF_8))
                val ligne = lecteur.readLine() ?: return
                val morceaux = ligne.split(" ")
                if (morceaux.size < 2) {
                    Reponse(400, json(false, "Requête invalide"))
                } else {
                    val entetes = mutableMapOf<String, String>()
                    while (true) {
                        val l = lecteur.readLine() ?: break
                        if (l.isEmpty()) break
                        val i = l.indexOf(':')
                        if (i > 0) entetes[l.substring(0, i).trim().lowercase()] = l.substring(i + 1).trim()
                    }
                    val parties = morceaux[1].split("?", limit = 2)
                    val chemin = parties[0]
                    val requete = parties.getOrElse(1) { "" }
                    if (morceaux[0] != "GET" && morceaux[0] != "POST") Reponse(405, json(false, "Méthode non gérée"))
                    else gestionnaire(Requete(morceaux[0], decoder(chemin), analyserRequete(requete), entetes))
                }
            } catch (e: Exception) {
                Reponse(500, json(false, "Erreur interne : ${e.message}"))
            }
            val corps = reponse.json.toByteArray(Charsets.UTF_8)
            val texte = when (reponse.code) {
                200 -> "OK"; 400 -> "Bad Request"; 401 -> "Unauthorized"; 404 -> "Not Found"; 405 -> "Method Not Allowed"
                else -> "Error"
            }
            val sortie = c.getOutputStream()
            sortie.write(
                ("HTTP/1.1 ${reponse.code} $texte\r\nContent-Type: application/json; charset=utf-8\r\n" +
                    "Content-Length: ${corps.size}\r\nConnection: close\r\n\r\n").toByteArray(Charsets.US_ASCII)
            )
            sortie.write(corps)
            sortie.flush()
        }
    }

    companion object {
        fun analyserRequete(q: String): Map<String, String> =
            q.split("&").filter { it.isNotEmpty() }.associate {
                val kv = it.split("=", limit = 2)
                decoder(kv[0]) to decoder(kv.getOrElse(1) { "" })
            }

        private fun decoder(s: String): String = URLDecoder.decode(s, "UTF-8")

        fun json(ok: Boolean, message: String): String =
            "{\"ok\":$ok,\"message\":\"" +
                message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"}"
    }
}
