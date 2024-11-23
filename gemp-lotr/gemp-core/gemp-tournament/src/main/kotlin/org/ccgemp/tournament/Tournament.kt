package org.ccgemp.tournament

import java.time.LocalDateTime

data class Tournament(
    val tournamentId: String,
    val name: String,
    val startDate: LocalDateTime,
    val type: String,
    val parameters: String,
    val stage: String = "",
    val round: Int = 0,
)
