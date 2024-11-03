package org.ccgemp.tournament

interface TournamentRepository {
    fun getUnfinishedTournaments(): List<Tournament>

    fun getTournamentMatches(tournamentId: String): List<TournamentMatch>

    fun getTournamentPlayers(tournamentId: String): List<TournamentPlayer>

    fun setStage(tournamentId: String, stage: String)

    fun setRound(tournamentId: String, round: Int)

    fun createMatch(
        tournamentId: String,
        round: Int,
        playerOne: String,
        playerTwo: String,
        winner: String? = null,
    )

    fun dropPlayer(tournamentId: String, player: String)
}
