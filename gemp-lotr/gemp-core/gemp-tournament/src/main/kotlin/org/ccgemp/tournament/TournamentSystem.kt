package org.ccgemp.tournament

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.game.GameParticipant
import java.time.LocalDateTime

const val FINISHED_STAGE = "FINISHED"

@Exposes(UpdatedSystem::class, LifecycleObserver::class, TournamentInterface::class)
class TournamentSystem : TournamentInterface, UpdatedSystem, LifecycleObserver {
    @Inject
    private lateinit var repository: TournamentRepository

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<*>

    private val handlerMap = mutableMapOf<String, TournamentHandler<Any>>()
    private val loadedTournaments = mutableMapOf<String, DefaultTournamentInfo<Any>>()

    override fun afterContextStartup() {
        repository.getUnfinishedTournaments().forEach { tournament ->
            val tournamentHandler = findHandler(tournament.type)

            val data = tournamentHandler.initializeTournament(tournament)

            val tournamentInfo =
                DefaultTournamentInfo(
                    tournamentHandler,
                    tournament.startDate,
                    data,
                    tournament.tournamentId,
                    tournament.stage,
                    tournament.round,
                    mutableListOf(),
                    mutableListOf(),
                    mutableSetOf(),
                )

            val tournamentPlayers = repository.getTournamentPlayers(tournament.tournamentId)
            tournamentPlayers.forEach {
                tournamentInfo.players.add(it)
            }
            val tournamentMatches = repository.getTournamentMatches(tournament.tournamentId)
            tournamentMatches.forEach {
                tournamentInfo.matches.add(it)
            }

            loadedTournaments[tournament.tournamentId] = tournamentInfo

            // Start all the matches that are not finished
            tournamentMatches.filter { !it.finished && !it.bye }.forEach { match ->
                startMatch(match.round, match.playerOne, match.playerTwo, tournamentInfo)
            }
        }
    }

    private fun findHandler(type: String): TournamentHandler<Any> {
        val tournamentHandler =
            handlerMap[type.lowercase()] ?: throw IllegalStateException("Handler for tournament type $type not found")
        return tournamentHandler
    }

    override fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler<Any>) {
        handlerMap[type.lowercase()] = tournamentHandler
    }

    override fun update() {
        val tournamentsToUnload = mutableSetOf<String>()
        loadedTournaments.forEach { (tournamentId, tournamentInfo) ->
            val handler = tournamentInfo.handler
            if (tournamentInfo.stage != FINISHED_STAGE && tournamentInfo.startDate.isBefore(LocalDateTime.now())) {
                handler.progressTournament(tournamentInfo, DefaultTournamentProgress(tournamentInfo))
            }
            if (tournamentInfo.stage == FINISHED_STAGE) {
                handler.unloadTournament(tournamentInfo)
                tournamentsToUnload.add(tournamentId)
            }
        }
        tournamentsToUnload.forEach {
            loadedTournaments.remove(it)
        }
    }

    private fun startMatch(
        round: Int,
        playerOne: String,
        playerTwo: String,
        tournament: DefaultTournamentInfo<Any>,
    ) {
        val handler = tournament.handler
        val participants =
            arrayOf(
                GameParticipant(playerOne, handler.getPlayerDeck(tournament, playerOne, round)),
                GameParticipant(playerTwo, handler.getPlayerDeck(tournament, playerTwo, round)),
            )
        val gameId = gameContainerInterface.createNewGame(participants, handler.getGameSettings(tournament, round))
        tournament.runningGames.add(gameId)
    }

    inner class DefaultTournamentProgress(
        private val info: DefaultTournamentInfo<Any>,
    ) : TournamentProgress {
        override fun updateState(round: Int, stage: String) {
            repository.setStage(info.id, stage)
            repository.setRound(info.id, round)
            info.stage = stage
            info.round = round
        }

        override fun createMatch(recipe: TournamentGameRecipe) {
            val playerOne = recipe.participants[0]
            val playerTwo = recipe.participants[1]
            val round = recipe.round

            repository.createMatch(info.id, round, playerOne, playerTwo)

            startMatch(round, playerOne, playerTwo, info)

            info.matches.add(TournamentMatch(info.id, recipe.round, playerOne, playerTwo, null))
        }

        override fun awardBye(round: Int, player: String) {
            repository.createMatch(info.id, round, player, BYE_NAME, player)

            info.matches.add(TournamentMatch(info.id, round, player, BYE_NAME, player))
        }

        override fun dropPlayer(player: String) {
            repository.dropPlayer(info.id, player)
            info.players.firstOrNull { it.player == player }?.dropped = true
        }
    }

    data class DefaultTournamentInfo<TournamentData>(
        val handler: TournamentHandler<TournamentData>,
        val startDate: LocalDateTime,
        override val data: TournamentData,
        override val id: String,
        override var stage: String,
        override var round: Int,
        override val players: MutableList<TournamentPlayer>,
        override val matches: MutableList<TournamentMatch>,
        val runningGames: MutableSet<String>,
    ) : TournamentInfo<TournamentData>
}
