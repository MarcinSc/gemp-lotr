package org.ccgemp.tournament.composite.pairing

interface ManualPairing {
    fun pairRound(tournamentId: String, round: Int, pairing: RoundPairing): Boolean
}
