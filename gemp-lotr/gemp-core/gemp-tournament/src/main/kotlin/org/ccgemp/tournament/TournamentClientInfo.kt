package org.ccgemp.tournament

import java.time.LocalDateTime

interface TournamentClientInfo {
    val id: String
    val startDate: LocalDateTime
    val name: String
    val round: Int
    val status: String
    val players: List<TournamentParticipant>
    val matches: List<TournamentMatch>
    val finished: Boolean
}
