package org.ccgemp.tournament

import org.ccgemp.tournament.composite.CompositeTournamentHandlerSystem
import org.ccgemp.tournament.composite.matches.MatchTournamentProcesses
import org.ccgemp.tournament.composite.matches.kickoff.ManualKickoffProvider
import org.ccgemp.tournament.composite.matches.kickoff.TimedKickoffProvider
import org.ccgemp.tournament.composite.matches.kickoff.TournamentKickoffSystem
import org.ccgemp.tournament.composite.matches.pairing.ManualPairingProvider
import org.ccgemp.tournament.composite.matches.pairing.SwissPairingProvider
import org.ccgemp.tournament.composite.matches.pairing.TournamentPairingSystem
import org.ccgemp.tournament.composite.matches.standing.ModifiedMedianStandingsProvider
import org.ccgemp.tournament.composite.matches.standing.TournamentStandingsSystem
import org.ccgemp.tournament.composite.misc.MiscTournamentProcesses

fun createTournamentSystems(): List<Any> {
    return listOf(
        TournamentSystem(),
        DbTournamentRepository(),
        CompositeTournamentHandlerSystem(),
        MatchTournamentProcesses(),
        MiscTournamentProcesses(),
        // Kickoff
        TournamentKickoffSystem(),
        TimedKickoffProvider(),
        ManualKickoffProvider(),
        // Pairing
        TournamentPairingSystem(),
        SwissPairingProvider(),
        ManualPairingProvider(),
        // Standings
        TournamentStandingsSystem(),
        ModifiedMedianStandingsProvider(),
        // API
        TournamentApiSystem(),
        PrivateTournamentApiSystem(),
    )
}
