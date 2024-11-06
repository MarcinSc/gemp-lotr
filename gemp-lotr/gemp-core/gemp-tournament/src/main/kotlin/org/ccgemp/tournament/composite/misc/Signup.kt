package org.ccgemp.tournament.composite.misc

import org.ccgemp.deck.DeckValidator
import org.ccgemp.deck.GameDeck
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.SignupTournamentProcess
import java.time.LocalDateTime

const val SIGNUP_OPEN = "SIGNUP_OPEN"

class Signup(
    private val allowedPlayers: Set<String>?,
    private val validators: List<DeckValidator>,
    private val until: LocalDateTime,
) : SignupTournamentProcess {
    override fun canJoinTournament(players: List<TournamentParticipant>, player: String, decks: List<GameDeck?>): Boolean {
        return players.none { it.player == player } &&
            (allowedPlayers == null || allowedPlayers.contains(player)) &&
            run {
                validators.withIndex().all {
                    it.value.isValid(decks[it.index])
                }
            }
    }

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
            "" -> tournamentProgress.updateState(round, SIGNUP_OPEN)
            SIGNUP_OPEN -> {
                if (until.isAfter(LocalDateTime.now())) {
                    tournamentProgress.updateState(round, FINISHED_STAGE)
                }
            }
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return "Signup open"
    }
}
