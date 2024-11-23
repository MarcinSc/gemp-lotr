package org.ccgemp.tournament.composite.standing

data class PlayerStanding(
    val name: String,
    val standing: Int,
    val points: Int,
    val stats: Map<String, Number>,
)
