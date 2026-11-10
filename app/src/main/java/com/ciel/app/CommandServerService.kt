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
    private val commandProcessor = CielCommandProcessor()

    override fun onCreate() {
        super.onCreate()
        server = embeddedServer(Netty, port = 8080) {
            routing {
                get("/status") {
                    call.respondText("Server is running")
                }
                
                post("/command") {
                    val authHeader = call.request.header(HttpHeaders.Authorization)
                    val body = call.receiveText()
                    
                    when (val result = commandProcessor.processCommand(authHeader, body)) {
                        is CielCommandProcessor.CommandResult.Unauthorized -> {
                            call.respond(HttpStatusCode.Unauthorized, "Accès refusé")
                        }
                        is CielCommandProcessor.CommandResult.Success -> {
                            call.respondText(result.message)
                        }
                        is CielCommandProcessor.CommandResult.Error -> {
                            call.respond(HttpStatusCode.BadRequest, result.message)
                        }
                    }
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
