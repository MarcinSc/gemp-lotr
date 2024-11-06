package org.ccgemp.tournament.composite

import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentPlayer
import org.ccgemp.tournament.TournamentProgress

interface TournamentProcess {
    fun processTournament(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
        tournamentProgress: TournamentProgress,
    )

    fun getTournamentStatus(stage: String): String
}
