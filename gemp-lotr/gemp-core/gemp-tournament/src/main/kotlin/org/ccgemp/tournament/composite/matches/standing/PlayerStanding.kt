package org.ccgemp.tournament.composite.matches.standing

data class PlayerStanding(
    val name: String,
    val standing: Int,
    val points: Int,
    val stats: Map<String, Number>,
)
