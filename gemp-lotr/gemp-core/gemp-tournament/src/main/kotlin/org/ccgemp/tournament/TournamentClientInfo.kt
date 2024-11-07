package org.ccgemp.tournament

import java.time.LocalDateTime

interface TournamentClientInfo {
    val id: String
    val startDate: LocalDateTime
    val name: String
    val status: String
    val players: List<TournamentParticipant>
    val finished: Boolean
}
