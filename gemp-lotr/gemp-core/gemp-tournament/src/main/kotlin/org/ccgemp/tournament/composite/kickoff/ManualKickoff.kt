package org.ccgemp.tournament.composite.kickoff

interface ManualKickoff {
    fun kickoffRound(tournamentId: String, round: Int): Boolean
}
