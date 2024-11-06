package org.ccgemp.tournament

import java.time.LocalDateTime

interface TournamentRepository {
    fun getUnfinishedOrStartAfter(time: LocalDateTime): List<Tournament>

    fun getTournamentMatches(tournamentId: String): List<TournamentMatch>

    fun getTournamentPlayers(tournamentId: String): List<TournamentPlayer>

    fun setRoundAndStage(tournamentId: String, round: Int, stage: String)

    fun createMatch(
        tournamentId: String,
        round: Int,
        playerOne: String,
        playerTwo: String,
        winner: String? = null,
    )

    fun addPlayer(tournamentId: String, player: String, deck: String)

    fun dropPlayer(tournamentId: String, player: String)

    fun updateDecks(tournamentId: String, player: String, deck: String)
}
