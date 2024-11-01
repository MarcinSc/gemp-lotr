package org.ccgemp.tournament

interface TournamentHandler {
    fun initializeTournament(tournament: Tournament): LoadedTournament

    fun progressTournament(tournament: LoadedTournament): List<TournamentGameRecipe>

    fun unloadTournament(tournament: LoadedTournament)
}
