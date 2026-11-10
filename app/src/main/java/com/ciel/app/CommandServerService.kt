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

class CommandServerService : Service() {
    private var server: NettyApplicationEngine? = null

    override fun onCreate() {
        super.onCreate()
        server = embeddedServer(Netty, port = 8080) {
            routing {
                post("/command") {
                    val body = call.receiveText()
                    // Logique de traitement de la commande de Ciel ici
                    call.respondText("Command received: $body")
                }
                get("/status") {
                    call.respondText("Server is running")
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
