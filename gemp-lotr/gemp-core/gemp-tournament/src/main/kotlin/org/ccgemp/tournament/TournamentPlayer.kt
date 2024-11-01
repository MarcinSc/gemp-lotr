package org.ccgemp.tournament

data class TournamentPlayer(
    val id: Int = 0,
    val tournamentId: String,
    val player: String,
    val deckName: String,
    val deck: String,
    val dropped: Boolean = false,
)
