package org.ccgemp.tournament.composite.misc

import org.ccgemp.common.TimeProvider
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.TournamentProcess
import java.time.Duration
import java.time.LocalDateTime

const val PAUSED = "PAUSED"

class Pause(
    private val timeProvider: TimeProvider,
    private val time: Duration,
) : TournamentProcess {
    private var pauseStart: LocalDateTime = timeProvider.now()

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
                pauseStart = timeProvider.now()
                tournamentProgress.updateState(round, PAUSED)
            }

            PAUSED -> {
                if (pauseStart.plus(time).isBefore(timeProvider.now())) {
                    tournamentProgress.updateState(round, FINISHED_STAGE)
                }
            }
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return "Paused"
    }
}
