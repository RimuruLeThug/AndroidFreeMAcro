package com.ciel.app

/**
 * Logique du « Mode Silence Intelligent ».
 * Décide si une commande doit être ignorée en fonction de l'état du mode « Ne pas déranger ».
 */
object IntelligentSilence {
    fun doitAnnuler(commande: Commande, respecterDnd: Boolean, dndActif: Boolean): Boolean {
        // Seules les alarmes et minuteurs sont concernés (le statut doit toujours répondre)
        if (commande !is Commande.Alarme && commande !is Commande.Minuteur) return false
        
        return respecterDnd && dndActif
    }
    
    const val MESSAGE_ANNULATION = "Action annulée : mode « Ne pas déranger » actif."
}
