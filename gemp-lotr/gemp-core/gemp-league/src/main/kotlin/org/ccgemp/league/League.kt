package org.ccgemp.league

import java.time.LocalDateTime

data class League(
    val leagueId: String,
    val name: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val type: String,
    val parameters: String,
)
