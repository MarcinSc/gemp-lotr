package org.ccgemp.tournament

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.game.GameParticipant

@Exposes(UpdatedSystem::class, LifecycleObserver::class, TournamentInterface::class)
class TournamentSystem : TournamentInterface, UpdatedSystem, LifecycleObserver {
    @Inject
    private lateinit var repository: TournamentRepository

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<*>

    private val handlerMap = mutableMapOf<String, TournamentHandler>()
    private val loadedTournaments = mutableMapOf<String, LoadedTournament>()
    private val runningGames = mutableMapOf<String, LoadedTournament>()

    override fun afterContextStartup() {
        repository.getUnfinishedTournaments().forEach { tournament ->
            val tournamentHandler = findHandler(tournament.type)
            val tournamentMatches = repository.getTournamentMatches(tournament.tournamentId)
            val tournamentPlayers = repository.getTournamentPlayers(tournament.tournamentId)

            val loadedTournament = tournamentHandler.initializeTournament(tournament, tournamentPlayers, tournamentMatches)
            loadedTournaments[tournament.tournamentId] = loadedTournament

            // Start all the matches that are not finished
            tournamentMatches.filter { !it.finished && !it.bye }.forEach { match ->
                createMatch(match.round, match.playerOne, match.playerTwo, loadedTournament)
            }
        }
    }

    private fun findHandler(type: String): TournamentHandler {
        val tournamentHandler =
            handlerMap[type.lowercase()] ?: throw IllegalStateException("Handler for tournament type $type not found")
        return tournamentHandler
    }

    override fun registerTournamentHandler(type: String, tournamentHandler: TournamentHandler) {
        handlerMap[type.lowercase()] = tournamentHandler
    }

    override fun update() {
        val tournamentsToUnload = mutableSetOf<String>()
        loadedTournaments.forEach { (tournamentId, tournament) ->
            if (!tournament.finished) {
                tournament.handler.progressTournament(tournament, DefaultTournamentProgress(tournamentId))
            }
            if (tournament.finished) {
                tournament.handler.unloadTournament(tournament)
                tournamentsToUnload.add(tournamentId)
            }
        }
        tournamentsToUnload.forEach {
            loadedTournaments.remove(it)
        }
    }

    private fun createMatch(
        round: Int,
        playerOne: String,
        playerTwo: String,
        tournament: LoadedTournament,
    ) {
        val participants =
            arrayOf(
                GameParticipant(playerOne, tournament.getPlayerDeck(playerOne, round)),
                GameParticipant(playerTwo, tournament.getPlayerDeck(playerTwo, round)),
            )
        val gameId = gameContainerInterface.createNewGame(participants, tournament.getGameSettings(round))
        runningGames[gameId] = tournament
    }

    inner class DefaultTournamentProgress(
        private val tournamentId: String,
    ) : TournamentProgress {
        override fun updateStage(stage: String) {
            repository.setStage(tournamentId, stage)
        }

        override fun setRound(round: Int) {
            repository.setRound(tournamentId, round)
        }

        override fun createMatch(recipe: TournamentGameRecipe) {
            val playerOne = recipe.participants[0]
            val playerTwo = recipe.participants[1]
            val round = recipe.round

            repository.createMatch(tournamentId, round, playerOne, playerTwo)

            val tournament = loadedTournaments[tournamentId]!!

            createMatch(round, playerOne, playerTwo, tournament)
        }

        override fun awardBye(round: Int, player: String) {
            repository.createMatch(tournamentId, round, player, BYE_NAME, player)
        }
    }
}
