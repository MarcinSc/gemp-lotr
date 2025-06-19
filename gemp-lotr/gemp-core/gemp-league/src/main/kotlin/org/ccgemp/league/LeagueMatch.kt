package org.ccgemp.league

class LeagueMatch(
    val playerOne: String,
    val playerTwo: String,
    var winner: String?,
    ) {
    val finished = winner != null
    val loser = if (winner != null) (listOf(playerOne, playerTwo) - winner).first() else null
    val players = listOf(playerOne, playerTwo)

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
