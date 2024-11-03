package org.ccgemp.tournament

const val BYE_NAME = "bye"

data class TournamentMatch(
    val tournamentId: String,
    val round: Int = 0,
    val playerOne: String,
    val playerTwo: String,
    val winner: String?,
) {
    val bye = playerTwo == BYE_NAME
    val finished = winner != null
}
