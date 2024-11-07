package org.ccgemp.tournament

data class TournamentDeck(
    val tournamentId: String,
    val player: String,
    val type: String,
    val name: String,
    val notes: String,
    val targetFormat: String,
    val contents: String,
)
