package org.ccgemp.tournament

import org.ccgemp.tournament.composite.CompositeTournamentHandlerSystem
import org.ccgemp.tournament.composite.deckbuilding.DeckBuildingTournamentProcesses
import org.ccgemp.tournament.composite.kickoff.ManualKickoffProvider
import org.ccgemp.tournament.composite.kickoff.TimedKickoffProvider
import org.ccgemp.tournament.composite.kickoff.TournamentKickoffSystem
import org.ccgemp.tournament.composite.matches.MatchTournamentProcesses
import org.ccgemp.tournament.composite.misc.MiscTournamentProcesses
import org.ccgemp.tournament.composite.pairing.ManualPairingProvider
import org.ccgemp.tournament.composite.pairing.SwissPairingProvider
import org.ccgemp.tournament.composite.pairing.TournamentPairingSystem
import org.ccgemp.tournament.composite.standing.ModifiedMedianStandingsProvider
import org.ccgemp.tournament.composite.standing.RoundLimitStandingsProvider
import org.ccgemp.tournament.composite.standing.SurvivorStandingsProvider
import org.ccgemp.tournament.composite.standing.TournamentStandingsSystem
import org.ccgemp.tournament.renderer.JsonTournamentModelRenderer
import org.ccgemp.tournament.renderer.TournamentModelRenderer

fun createTournamentSystems(tournamentModelRenderer: TournamentModelRenderer = JsonTournamentModelRenderer()): List<Any> {
    return listOf(
        TournamentSystem(),
        DbTournamentRepository(),
        CompositeTournamentHandlerSystem(),
        DeckBuildingTournamentProcesses(),
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
        SurvivorStandingsProvider(),
        RoundLimitStandingsProvider(),
        // API
        PublicTournamentApiSystem(),
        PrivateTournamentApiSystem(),
        AdminTournamentApiSystem(),
        tournamentModelRenderer,
    )
}
