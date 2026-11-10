package com.ciel.app

class CielCommandProcessor(private val apiToken: String = "Bearer CielSecureToken2026") {

    sealed class CommandResult {
        object Unauthorized : CommandResult()
        data class Success(val message: String) : CommandResult()
        data class Error(val message: String) : CommandResult()
    }

    fun isAuthorized(authHeader: String?): Boolean {
        return authHeader == apiToken
    }

    fun processCommand(authHeader: String?, body: String): CommandResult {
        if (!isAuthorized(authHeader)) {
            return CommandResult.Unauthorized
        }

        // Simplistic routing logic based on content
        return when {
            body.contains("alarme", ignoreCase = true) -> CommandResult.Success("Traitement de l'alarme: $body")
            body.contains("minuteur", ignoreCase = true) -> CommandResult.Success("Traitement du minuteur: $body")
            else -> CommandResult.Success("Commande générale traitée: $body")
        }
    }
}
