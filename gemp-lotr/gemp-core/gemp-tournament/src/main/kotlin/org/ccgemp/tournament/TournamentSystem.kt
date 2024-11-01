package org.ccgemp.tournament

import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import org.ccgemp.game.GameContainerInterface

@Exposes(UpdatedSystem::class, LifecycleObserver::class, TournamentInterface::class)
class TournamentSystem : TournamentInterface, UpdatedSystem, LifecycleObserver {
    @Inject
    private lateinit var repository: TournamentRepository

    @Inject
    private lateinit var gameContainerInterface: GameContainerInterface<*>

    private val handlerMap = mutableMapOf<String, TournamentHandler>()
    private val loadedTournaments = mutableMapOf<String, LoadedTournament>()
    private val runningGames = mutableMapOf<String, MutableSet<String>>()

    override fun afterContextStartup() {
        repository.getUnfinishedTournaments().forEach { tournament ->
            val tournamentHandler = findHandler(tournament.type)
            loadedTournaments[tournament.tournamentId] = tournamentHandler.initializeTournament(tournament)
            run
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
                val gamesToCreate = tournament.handler.progressTournament(tournament)
                gamesToCreate.forEach {
                    val gameId = gameContainerInterface.createNewGame(it.participants, it.gameSettings)
                    runningGames[tournamentId]?.add(gameId)
                }
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
}
