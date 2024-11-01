package org.ccgemp.tournament.sealed

import org.ccgemp.tournament.LoadedTournament
import org.ccgemp.tournament.Tournament
import org.ccgemp.tournament.TournamentHandler
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentPlayer
import org.ccgemp.tournament.TournamentProgress

const val SEALED_TOURNAMENT_TYPE: String = "SEALED"

class SealedTournamentHandler : TournamentHandler {
    override fun initializeTournament(tournament: Tournament, players: List<TournamentPlayer>, matches: List<TournamentMatch>): LoadedTournament {
        TODO("Not yet implemented")
    }

    override fun progressTournament(tournament: LoadedTournament, tournamentProgress: TournamentProgress) {
        TODO("Not yet implemented")
    }

    override fun unloadTournament(tournament: LoadedTournament) {
        TODO("Not yet implemented")
    }
}
