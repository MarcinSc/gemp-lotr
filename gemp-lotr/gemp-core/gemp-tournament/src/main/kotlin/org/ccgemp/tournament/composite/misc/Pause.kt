package org.ccgemp.tournament.composite.misc

import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentPlayer
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.TournamentProcess

const val PAUSED = "PAUSED"

class Pause(
    private val time: Long,
) : TournamentProcess {
    private var pauseStart: Long = 0

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
                pauseStart = System.currentTimeMillis()
                tournamentProgress.updateState(round, PAUSED)
            }

            PAUSED -> {
                if (pauseStart + time <= System.currentTimeMillis()) {
                    tournamentProgress.updateState(round, FINISHED_STAGE)
                }
            }
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return "Paused"
    }
}
