package com.ciel.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IntelligentSilenceTest {

    @Test fun nePasAnnulerStatut() {
        assertFalse(IntelligentSilence.doitAnnuler(Commande.Statut, true, true))
        assertFalse(IntelligentSilence.doitAnnuler(Commande.Statut, true, false))
        assertFalse(IntelligentSilence.doitAnnuler(Commande.Statut, false, true))
        assertFalse(IntelligentSilence.doitAnnuler(Commande.Statut, false, false))
    }

    @Test fun annulerAlarmeQuandDndActifEtRespecte() {
        val alarme = Commande.Alarme(7, 30, "Réveil")
        assertTrue(IntelligentSilence.doitAnnuler(alarme, true, true))
    }

    @Test fun nePasAnnulerAlarmeQuandDndInactif() {
        val alarme = Commande.Alarme(7, 30, "Réveil")
        assertFalse(IntelligentSilence.doitAnnuler(alarme, true, false))
    }

    @Test fun nePasAnnulerAlarmeQuandRespecteDndDesactive() {
        val alarme = Commande.Alarme(7, 30, "Réveil")
        assertFalse(IntelligentSilence.doitAnnuler(alarme, false, true))
    }

    @Test fun annulerMinuteurQuandDndActifEtRespecte() {
        val minuteur = Commande.Minuteur(60, "Test")
        assertTrue(IntelligentSilence.doitAnnuler(minuteur, true, true))
    }

    @Test fun nePasAnnulerMinuteurQuandDndInactif() {
        val minuteur = Commande.Minuteur(60, "Test")
        assertFalse(IntelligentSilence.doitAnnuler(minuteur, true, false))
    }

    @Test fun nePasAnnulerMinuteurQuandRespecteDndDesactive() {
        val minuteur = Commande.Minuteur(60, "Test")
        assertFalse(IntelligentSilence.doitAnnuler(minuteur, false, true))
    }

    @Test fun messageAnnulation() {
        assertEquals("Action annulée : mode « Ne pas déranger » actif.", IntelligentSilence.MESSAGE_ANNULATION)
    }
}