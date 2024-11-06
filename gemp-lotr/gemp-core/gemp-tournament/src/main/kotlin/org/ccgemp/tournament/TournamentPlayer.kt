package org.ccgemp.tournament

data class TournamentPlayer(
    val player: String,
    val decks: String,
    var dropped: Boolean = false,
)
