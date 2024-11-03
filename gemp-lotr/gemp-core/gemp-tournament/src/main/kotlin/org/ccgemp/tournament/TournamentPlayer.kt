package org.ccgemp.tournament

data class TournamentPlayer(
    val id: Int = 0,
    val tournamentId: String,
    val player: String,
    val deck: String,
    var dropped: Boolean = false,
)
