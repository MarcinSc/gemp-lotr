package org.ccgemp.tournament.composite.misc

import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.TournamentProcess
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PauseUntil(
    private val until: LocalDateTime,
) : TournamentProcess {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun processTournament(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        matches: List<TournamentMatch>,
        pairingGroups: Map<Int, String>,
        byeGroups: Map<Int, String>,
        tournamentProgress: TournamentProgress,
    ) {
        when (stage) {
            "" -> {
                if (!LocalDateTime.now().isBefore(until)) {
                    tournamentProgress.updateState(round, FINISHED_STAGE)
                }
            }
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return "Paused until - " + until.format(formatter)
    }
}
