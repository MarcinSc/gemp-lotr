package org.ccgemp.tournament

import java.time.LocalDateTime

interface TournamentInterface {
    fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler<Any>)

    fun addTournament(
        tournamentId: String,
        type: String,
        name: String,
        startDate: LocalDateTime,
        parameters: String,
    ): Boolean

    fun getLiveTournaments(): List<TournamentClientInfo>

    fun getHistoricTournaments(): List<TournamentClientInfo>

    fun getTournament(tournamentId: String): TournamentClientInfo?

    fun joinTournament(tournamentId: String, player: String, deckNames: List<String>)

    fun leaveTournament(tournamentId: String, player: String)

    fun registerDecks(tournamentId: String, player: String, deckNames: List<String>)

    fun setPlayerDrop(tournamentId: String, player: String, drop: Boolean): Boolean

    fun setPlayerDeck(
        tournamentId: String,
        player: String,
        deckType: String,
        deckName: String,
    ): Boolean
}
