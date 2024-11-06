package org.ccgemp.tournament

interface TournamentInfo<TournamentData> {
    val id: String
    val stage: String
    val round: Int
    val players: List<TournamentParticipant>
    val matches: List<TournamentMatch>
    val data: TournamentData
}
