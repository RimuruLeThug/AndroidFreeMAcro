package com.ciel.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CielCommandProcessorTest {

    private val processor = CielCommandProcessor()
    private val validToken = "Bearer CielSecureToken2026"

    @Test
    fun testUnauthorizedToken() {
        val result = processor.processCommand("Bearer InvalidToken", "alarme 07:30")
        assertTrue(result is CielCommandProcessor.CommandResult.Unauthorized)
    }

    @Test
    fun testNullToken() {
        val result = processor.processCommand(null, "alarme 07:30")
        assertTrue(result is CielCommandProcessor.CommandResult.Unauthorized)
    }

    @Test
    fun testNominalAlarmCommand() {
        val result = processor.processCommand(validToken, "alarme 07:30 Réveil")
        assertTrue(result is CielCommandProcessor.CommandResult.Success)
        val success = result as CielCommandProcessor.CommandResult.Success
        assertTrue(success.message.contains("alarme", ignoreCase = true))
    }

    @Test
    fun testNominalTimerCommand() {
        val result = processor.processCommand(validToken, "minuteur 300 Pâtes")
        assertTrue(result is CielCommandProcessor.CommandResult.Success)
        val success = result as CielCommandProcessor.CommandResult.Success
        assertTrue(success.message.contains("minuteur", ignoreCase = true))
    }

    @Test
    fun testCaseInsensitivityAlarm() {
        val result = processor.processCommand(validToken, "ALARME 08:00")
        assertTrue(result is CielCommandProcessor.CommandResult.Success)
    }

    @Test
    fun testCaseInsensitivityTimer() {
        val result = processor.processCommand(validToken, "MINUTEUR 60")
        assertTrue(result is CielCommandProcessor.CommandResult.Success)
    }

    @Test
    fun testGeneralCommand() {
        val result = processor.processCommand(validToken, "status")
        assertTrue(result is CielCommandProcessor.CommandResult.Success)
    }
}
