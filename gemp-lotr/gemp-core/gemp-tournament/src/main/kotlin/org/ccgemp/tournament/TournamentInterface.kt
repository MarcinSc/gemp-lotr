package org.ccgemp.tournament

import org.ccgemp.game.GameDeck

interface TournamentInterface {
    fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler<Any>)

    fun getLiveTournaments(): List<TournamentClientInfo>

    fun getHistoricTournaments(): List<TournamentClientInfo>

    fun getTournament(tournamentId: String): TournamentClientInfo?

    fun getDecks(tournamentId: String, player: String): List<GameDeck>
}
