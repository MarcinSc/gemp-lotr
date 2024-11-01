package org.ccgemp.tournament

interface TournamentInterface {
    fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler)
}
