package org.ccgemp.tournament

import org.ccgemp.deck.GameDeck

interface TournamentInterface {
    fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler<Any>)

    fun getLiveTournaments(): List<TournamentClientInfo>

    fun getHistoricTournaments(): List<TournamentClientInfo>

    fun getTournament(tournamentId: String): TournamentClientInfo?

    fun joinTournament(tournamentId: String, player: String, deckNames: List<String>, forced: Boolean = false)

    fun leaveTournament(tournamentId: String, player: String)

    fun registerDeck(tournamentId: String, player: String, deckName: String, forced: Boolean = false)
}
