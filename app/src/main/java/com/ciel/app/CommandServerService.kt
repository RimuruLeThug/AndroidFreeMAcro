package com.ciel.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*

class CommandServerService : Service() {
    private var server: NettyApplicationEngine? = null
    
    // Ton token secret
    private val API_TOKEN=*** CielSecureToken2026" 

    private fun isAuthorized(call: ApplicationCall): Boolean {
        val authHeader = call.request.header(HttpHeaders.Authorization)
        return authHeader == API_TOKEN
    }

    override fun onCreate() {
        super.onCreate()
        server = embeddedServer(Netty, port = 8080) {
            routing {
                get("/status") {
                    call.respondText("Server is running")
                }
                
                post("/command") {
                    if (!isAuthorized(call)) {
                        call.respond(HttpStatusCode.Unauthorized, "Accès refusé")
                        return@post
                    }
                    val body = call.receiveText()
                    call.respondText("Commande traitée sécurisée: $body")
                }
            }
        }.start(wait = false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        server?.stop(1000, 5000)
        super.onDestroy()
    }
}