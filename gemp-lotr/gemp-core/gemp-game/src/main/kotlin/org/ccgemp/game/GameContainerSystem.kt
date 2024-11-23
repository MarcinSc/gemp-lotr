package org.ccgemp.game

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectList
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.server.generateUniqueId
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Exposes(GameContainerInterface::class, LifecycleObserver::class, UpdatedSystem::class)
class GameContainerSystem : GameContainerInterface<Any>, LifecycleObserver, UpdatedSystem {
    @Inject
    private lateinit var gameProducer: GameProducer

    @InjectList
    private lateinit var gameResultListener: List<GameResultListener>

    @InjectValue("games.threadCount")
    private var gamesThreadCount: Int = 1

    @InjectValue("games.lingerTime")
    private var gamesLingerTime: Long = 1000 * 60 * 5

    private val gameContainers = mutableListOf<GameContainer>()
    private val games: MutableMap<String, GameEntry> = mutableMapOf()

    override fun afterContextStartup() {
        (1..gamesThreadCount).forEach { _ ->
            gameContainers.add(GameContainer(Executors.newSingleThreadExecutor()))
        }
    }

    override fun beforeContextStopped() {
        gameContainers.forEach {
            it.executorService.shutdown()
        }
        gameContainers.clear()
    }

    override fun update() {
        val currentTime = System.currentTimeMillis()
        val gamesToRemove = mutableSetOf<String>()

        games.forEach { (gameId, gameEntry) ->
            val gameContainer = findGameContainer(gameId)

            val gameResult = gameEntry.game.gameResult
            if (gameResult != null && !gameEntry.notifiedFinished) {
                gameResultListener.forEach {
                    if (gameResult.cancelled) {
                        it.gameCancelled(gameId)
                    } else {
                        it.gameFinished(gameId, gameEntry.game.gameParticipants, gameResult.winner!!)
                    }
                }
                gameEntry.notifiedFinished = true
            }
            if (gameResult != null && currentTime > gameResult.finishTime + gamesLingerTime) {
                gameContainer?.executorService?.execute {
                    gameEntry.game.finalizeGame()
                    gameContainer.games.remove(gameId)
                    gamesToRemove.add(gameId)
                }
            }
            if (gameResult == null) {
                gameContainer?.executorService?.execute {
                    gameEntry.game.checkForTimeouts()
                }
            }
        }
        gamesToRemove.forEach {
            games.remove(it)
        }
    }

    override fun createNewGame(participants: Array<GameParticipant>, gameSettings: GameSettings): String {
        val gameContainer = findBestContainer()
        val gameId = generateUniqueId()
        val game = gameProducer.createGame(participants, gameSettings)
        games[gameId] = GameEntry(game, false)
        gameContainer.games.add(gameId)
        return gameId
    }

    override fun joinGame(
        gameId: String,
        playerId: String,
        admin: Boolean,
        gameStream: GameStream<Any>,
    ): Runnable? {
        val gameEntry = games[gameId] ?: return null
        val game = gameEntry.game

        val gameSettings = game.gameSettings
        if (gameSettings.private && !admin && !game.gameParticipants.any { it.playerId == playerId }) {
            return null
        }

        val gameContainer = findGameContainer(gameId)
        val channelId = generateUniqueId()
        if (gameContainer != null) {
            gameContainer.executorService.execute {
                joinGameInGameThread(game, playerId, channelId, gameStream)
            }
            return Runnable {
                gameContainer.executorService.execute {
                    leaveGameInGameThread(game, channelId)
                }
            }
        }
        return null
    }

    override fun processPlayerDecision(
        gameId: String,
        playerId: String,
        decisionId: String,
        decisionValue: String,
    ): Boolean {
        val gameEntry = games[gameId] ?: return false

        val container = findGameContainer(gameId)
        return container?.let {
            it.executorService.execute {
                processDecisionInGameThread(gameEntry.game, playerId, decisionId, decisionValue)
            }
            true
        } ?: false
    }

    private fun findGameContainer(gameId: String): GameContainer? {
        val container =
            gameContainers.firstOrNull {
                it.games.contains(gameId)
            }
        return container
    }

    private fun findBestContainer(): GameContainer = gameContainers.minBy { it.games.size }

    private fun processDecisionInGameThread(
        game: Game,
        playerId: String,
        decisionId: String,
        decisionValue: String,
    ) {
        game.processDecision(playerId, decisionId, decisionValue)
    }

    private fun joinGameInGameThread(
        game: Game,
        playerId: String,
        channelId: String,
        gameStream: GameStream<Any>,
    ) {
        game.joinGame(playerId, channelId, gameStream)
    }

    private fun leaveGameInGameThread(game: Game, channelId: String) {
        game.leaveGame(channelId)
    }
}

internal class GameContainer(
    val executorService: ExecutorService,
    val games: MutableSet<String> = mutableSetOf(),
)

internal data class GameEntry(
    val game: Game,
    var notifiedFinished: Boolean,
)
