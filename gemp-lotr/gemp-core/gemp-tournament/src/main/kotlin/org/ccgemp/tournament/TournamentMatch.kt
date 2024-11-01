package org.ccgemp.tournament

const val BYE_NAME = "bye"

data class TournamentMatch(
    val id: Int = 0,
    val tournamentId: String,
    val round: Int = 0,
    val playerOne: String,
    val playerTwo: String,
    val winner: String?,
) {
    val bye = playerTwo == BYE_NAME
    val finished = winner != null
}
