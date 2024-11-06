package org.ccgemp.tournament.composite.matches

import org.ccgemp.game.GameSettings
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentGameRecipe
import org.ccgemp.tournament.TournamentMatch
import org.ccgemp.tournament.TournamentParticipant
import org.ccgemp.tournament.TournamentProgress
import org.ccgemp.tournament.composite.MatchesTournamentProcess
import org.ccgemp.tournament.composite.matches.kickoff.Kickoff
import org.ccgemp.tournament.composite.matches.pairing.Pairing

const val PLAYING_GAMES = "PLAYING_GAMES"
const val AWAITING_KICKOFF = "AWAITING_KICKOFF"

class PlayGamesTournamentProcess(
    private val startingRound: Int,
    override val rounds: Int,
    override val deckIndex: Int,
    override val gameSettings: GameSettings,
    override val pairingGroup: String,
    override val byeGroup: String,
    private val kickoff: Kickoff,
    private val pairing: Pairing,
    private val dropLosers: Boolean,
) : MatchesTournamentProcess {
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
                tournamentProgress.updateState(startingRound, AWAITING_KICKOFF)
            }

            AWAITING_KICKOFF -> {
                if (kickoff.isKickedOff(round) && pairing.isReady(round)) {
                    val roundPairing = pairing.createPairings(round, players, matches, pairingGroups, byeGroups)
                    if (roundPairing == null) {
                        // Can't pair - just finish this stage
                        tournamentProgress.updateState(startingRound + rounds - 1, FINISHED_STAGE)
                    } else {
                        roundPairing.pairings.forEach {
                            tournamentProgress.createMatch(
                                TournamentGameRecipe(round, arrayOf(it.first, it.second), gameSettings),
                            )
                        }
                        roundPairing.byes.forEach {
                            tournamentProgress.awardBye(round, it)
                        }
                        tournamentProgress.updateState(round, PLAYING_GAMES)
                    }
                }
            }

            PLAYING_GAMES -> {
                if (matches.none {
                        it.round == round && it.winner == null
                    }
                ) {
                    if (dropLosers) {
                        matches.filter { it.round == round && !it.bye }.forEach {
                            val loser = (listOf(it.playerOne, it.playerTwo) - it.winner!!).first()
                            tournamentProgress.dropPlayer(loser)
                        }
                    }
                    if (round + 1 - startingRound < rounds) {
                        tournamentProgress.updateState(round + 1, AWAITING_KICKOFF)
                    } else {
                        tournamentProgress.updateState(round, FINISHED_STAGE)
                    }
                }
            }
        }
    }

    override fun getTournamentStatus(stage: String): String {
        return when (stage) {
            "", AWAITING_KICKOFF -> "Awaiting Kickoff"
            PLAYING_GAMES -> "Playing Games"
            else -> "Unknown"
        }
    }
}
