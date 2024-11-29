package org.ccgemp.tournament.composite

import org.ccgemp.common.splitText
import org.ccgemp.common.GameDeck
import org.ccgemp.game.GameSettings
import org.ccgemp.tournament.FINISHED_STAGE
import org.ccgemp.tournament.TournamentGameRecipe
import org.ccgemp.tournament.TournamentInfo
import org.ccgemp.tournament.TournamentProgress

class TournamentPlan {
    private val processes: MutableList<TournamentProcess> = mutableListOf()
    private val roundMatchProcesses: MutableMap<Int, MatchesTournamentProcess> = mutableMapOf()
    private val pairingGroups: MutableMap<Int, String> = mutableMapOf()
    private val byeGroups: MutableMap<Int, String> = mutableMapOf()

    val rounds: Int
        get() =
            run {
                var result = 0
                processes.filterIsInstance<MatchesTournamentProcess>().forEach {
                    result += it.rounds
                }
                result
            }

    fun addProcess(process: TournamentProcess) {
        if (process is MatchesTournamentProcess) {
            val start = rounds
            (1..process.rounds).forEach {
                roundMatchProcesses[start + it] = process
                pairingGroups[start + it] = process.pairingGroup
                byeGroups[start + it] = process.byeGroup
            }
        }
        processes.add(process)
    }

    fun getRegisterDeckTypes(tournament: TournamentInfo<TournamentPlan>): List<String> {
        val processStage = parseProcessStage(tournament)
        val stage = processes[processStage.first]
        return if (stage is RegisterDeckTournamentProcess) {
            stage.deckTypes
        } else {
            emptyList()
        }
    }

    fun getPlayedDeckType(round: Int): String {
        return roundMatchProcesses[round]!!.deckType
    }

    fun getGameSettings(round: Int): GameSettings {
        return roundMatchProcesses[round]!!.gameSettings
    }

    fun canJoinTournament(tournament: TournamentInfo<TournamentPlan>, player: String): Boolean {
        val processStage = parseProcessStage(tournament)
        val stage = processes[processStage.first]
        return stage is SignupTournamentProcess && stage.canJoinTournament(tournament.players, player)
    }

    fun canRegisterDeck(tournament: TournamentInfo<TournamentPlan>, player: String, decks: List<GameDeck>): Boolean {
        val processStage = parseProcessStage(tournament)
        val stage = processes[processStage.first]
        return stage is RegisterDeckTournamentProcess && stage.canRegisterDecks(tournament.round, tournament.stage, tournament.players, player, decks)
    }

    fun progressTournament(tournament: TournamentInfo<TournamentPlan>, tournamentProgress: TournamentProgress) {
        val processStage = parseProcessStage(tournament)

        var processIndex = processStage.first
        val stage = processStage.second

        val innerProgress = OverridingTournamentProgress(tournamentProgress, stage, tournament.round)

        while (true) {
            processes[processIndex].processTournament(
                innerProgress.round,
                innerProgress.stage,
                tournament.players,
                tournament.matches,
                pairingGroups,
                byeGroups,
                innerProgress,
            )
            if (innerProgress.stage != FINISHED_STAGE) {
                break
            }
            processIndex++
            if (processIndex == processes.size) {
                break
            }
            innerProgress.updateState(innerProgress.round, "")
        }

        if (processIndex == processes.size) {
            tournamentProgress.updateState(innerProgress.round, FINISHED_STAGE)
        } else {
            val newStage = "$processIndex:${innerProgress.stage}"
            if (tournament.round != innerProgress.round || tournament.stage != newStage) {
                tournamentProgress.updateState(innerProgress.round, newStage)
            }
        }
    }

    private fun parseProcessStage(tournament: TournamentInfo<TournamentPlan>): Pair<Int, String> {
        return if (tournament.stage == "") {
            0 to ""
        } else {
            val stageSplit = tournament.stage.splitText(':', 2)
            stageSplit[0].toInt() to stageSplit[1]
        }
    }

    fun getTournamentStatus(tournament: TournamentInfo<TournamentPlan>): String {
        val processStage = parseProcessStage(tournament)

        return processes[processStage.first].getTournamentStatus(processStage.second)
    }

    internal class OverridingTournamentProgress(
        private val delegate: TournamentProgress,
        var stage: String,
        var round: Int,
    ) : TournamentProgress {
        override fun updateState(round: Int, stage: String) {
            this.round = round
            this.stage = stage
        }

        override fun createMatch(recipe: TournamentGameRecipe) {
            delegate.createMatch(recipe)
        }

        override fun awardBye(round: Int, player: String) {
            delegate.awardBye(round, player)
        }

        override fun dropPlayer(player: String) {
            delegate.dropPlayer(player)
        }
    }
}
