package org.ccgemp.tournament.sealed

import org.ccgemp.tournament.LoadedTournament
import org.ccgemp.tournament.Tournament
import org.ccgemp.tournament.TournamentHandler

val SEALED_TOURNAMENT_TYPE: String = "SEALED"

class SealedTournamentHandler : TournamentHandler {
    override fun initializeTournament(tournament: Tournament): LoadedTournament {
        TODO("Not yet implemented")
    }

    override fun progressTournament(tournament: LoadedTournament) {
        TODO("Not yet implemented")
    }

    override fun unloadTournament(tournament: LoadedTournament) {
        TODO("Not yet implemented")
    }
}
