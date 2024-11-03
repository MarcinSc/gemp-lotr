package org.ccgemp.tournament

import org.ccgemp.tournament.composite.CompositeTournamentHandlerSystem
import org.ccgemp.tournament.composite.matches.MatchTournamentProcesses
import org.ccgemp.tournament.composite.matches.kickoff.TimedKickoff
import org.ccgemp.tournament.composite.matches.kickoff.TournamentKickoffSystem
import org.ccgemp.tournament.composite.matches.pairing.TournamentPairingSystem

fun createSystems(): List<Any> {
    return listOf(
        TournamentSystem(),
        CompositeTournamentHandlerSystem(),
        MatchTournamentProcesses(),
        TournamentKickoffSystem(),
        TimedKickoff(),
        TournamentPairingSystem(),
    )
}
