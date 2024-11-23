package org.ccgemp.tournament

import java.time.LocalDateTime

interface TournamentRepository {
    fun getUnfinishedOrStartAfter(time: LocalDateTime): List<Tournament>

    fun getTournamentMatches(tournamentId: String): List<TournamentMatch>

    fun getTournamentPlayers(tournamentId: String): List<TournamentPlayer>

    fun getTournamentDecks(tournamentId: String): List<TournamentDeck>

    fun setRoundAndStage(tournamentId: String, round: Int, stage: String)

    fun createMatch(
        tournamentId: String,
        round: Int,
        playerOne: String,
        playerTwo: String,
        winner: String? = null,
    )

    fun addPlayer(tournamentId: String, player: String)

    fun setPlayerDrop(tournamentId: String, player: String, dropped: Boolean)

    fun upsertDeck(
        tournamentId: String,
        player: String,
        type: String,
        name: String,
        notes: String,
        targetFormat: String,
        contents: String,
    )
}
