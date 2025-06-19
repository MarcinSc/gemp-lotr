package org.ccgemp.tournament.composite.misc

import org.ccgemp.deck.DeckValidator
import org.ccgemp.deck.GameDeck
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.SignupTournamentProcess
import org.ccgemp.tournament.composite.kickoff.Kickoff

const val SIGNUP_OPEN = "SIGNUP_OPEN"

class Signup(
    private val allowedPlayers: Set<String>?,
    override val deckTypes: List<String>,
    private val validators: List<DeckValidator>,
    private val kickoff: Kickoff,
) : SignupTournamentProcess {
    override fun canJoinTournament(players: List<TournamentParticipant>, player: String): Boolean {
        return players.none { it.player == player } &&
            (allowedPlayers == null || allowedPlayers.contains(player))
    }

    override fun canRegisterDecks(
        round: Int,
        stage: String,
        players: List<TournamentParticipant>,
        player: String,
        decks: List<GameDeck>,
    ): Boolean {
        return validators.withIndex().all {
            it.value.isValid(player, decks[it.index])
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
                if (kickoff.isKickedOff(round)) {
                    tournamentProgress.updateState(round, FINISHED_STAGE)
                }
            }
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return "Signup open"
    }
}
