package org.ccgemp.tournament

const val BYE_NAME = "bye"

data class TournamentMatch(
    val round: Int = 0,
    val playerOne: String,
    val playerTwo: String,
    var winner: String?,
) {
    val bye = playerTwo == BYE_NAME
    val finished = winner != null
    val loser = if (winner != null && !bye) (listOf(playerOne, playerTwo) - winner).first() else null

    fun hasPlayer(name: String): Boolean {
        return playerOne == name || playerTwo == name
    }

    fun getOpponent(name: String): String? {
        return if (hasPlayer(name)) {
            if (playerOne == name) playerTwo else playerOne
        } else {
            null
        }
    }
}
