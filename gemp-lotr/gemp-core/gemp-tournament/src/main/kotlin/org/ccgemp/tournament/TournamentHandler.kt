package org.ccgemp.tournament

interface TournamentHandler {
    fun initializeTournament(tournament: Tournament, players: List<TournamentPlayer>, matches: List<TournamentMatch>): LoadedTournament

    fun progressTournament(tournament: LoadedTournament, tournamentProgress: TournamentProgress)

    fun unloadTournament(tournament: LoadedTournament)
}
