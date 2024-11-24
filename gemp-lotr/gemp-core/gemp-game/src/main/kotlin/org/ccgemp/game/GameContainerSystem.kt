package org.ccgemp.game

import com.gempukku.context.Registration
import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.initializer.inject.InjectList
import com.gempukku.context.initializer.inject.InjectValue
import com.gempukku.context.lifecycle.LifecycleObserver
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.context.update.UpdatedSystem
import com.gempukku.ostream.ObjectStream
import com.gempukku.server.HttpProcessingException
import com.gempukku.server.ResponseWriter
import com.gempukku.server.generateUniqueId
import org.ccgemp.state.ServerStateInterface
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Exposes(GameContainerInterface::class, LifecycleObserver::class, UpdatedSystem::class)
class GameContainerSystem : GameContainerInterface<Any, Any>, LifecycleObserver, UpdatedSystem {
    @Inject
    private lateinit var gameProducer: GameProducer<Any>

    @Inject(allowsNull = true)
    private var serverState: ServerStateInterface? = null

    @InjectList
    private lateinit var gameResultListener: List<GameResultListener>

    @InjectValue("games.threadCount")
    private var gamesThreadCount: Int = 1

    @InjectValue("games.lingerTime")
    private var gamesLingerTime: Long = 1000 * 60 * 5

    private val gameContainers = mutableListOf<GameContainer>()
    private val games: MutableMap<String, GameEntry> = mutableMapOf()

    private var playedGameStream: ObjectStream<PlayedGame>? = null
    private var finishedGameStream: ObjectStream<FinishedGame>? = null

    override fun afterContextStartup() {
        playedGameStream = serverState?.registerProducer("playedGame", PlayedGame::class)
        finishedGameStream = serverState?.registerProducer("finishedGame", FinishedGame::class)

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
                playedGameStream?.objectRemoved(gameId)
                gameEntry.notifiedFinished = true
                finishedGameStream?.objectCreated(gameId, gameEntry.game)
            }
            if (gameResult != null && currentTime > gameResult.finishTime + gamesLingerTime) {
                gameContainer?.executorService?.execute {
                    gameEntry.game.finalizeGame()
                    gameContainer.games.remove(gameId)
                    gamesToRemove.add(gameId)
                }
                finishedGameStream?.objectRemoved(gameId)
            }
            if (gameResult == null) {
                val status = gameEntry.game.status
                if (gameEntry.communicatedStatus != status) {
                    gameEntry.communicatedStatus = status
                    playedGameStream?.objectUpdated(gameId, gameEntry.game)
                }
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
        val game = gameProducer.createGame(gameId, participants, gameSettings)
        games[gameId] = GameEntry(game, game.status, false)
        gameContainer.games.add(gameId)
        playedGameStream?.objectCreated(gameId, game)
        return gameId
    }

    override fun setPlayerObserveSettings(gameId: String, player: String, settings: Any) {
        val gameEntry = games[gameId] ?: return
        val game = gameEntry.game
        val gameContainer = findGameContainer(gameId)
        gameContainer?.executorService?.execute {
            setPlayerObserveSettingsInThread(game, player, settings)
        }
    }

    override fun joinGame(
        gameId: String,
        playerId: String,
        admin: Boolean,
        gameStream: GameStream<Any>,
    ): Registration? {
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
            return object:Registration {
                override fun deregister() {
                    gameContainer.executorService.execute {
                        leaveGameInGameThread(game, channelId)
                    }
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

    override fun produceCardInfo(gameId: String, playerId: String, cardId: String, responseWriter: ResponseWriter) {
        val gameEntry = games[gameId] ?: throw HttpProcessingException(404)

        val container = findGameContainer(gameId)
        container?.let {
            it.executorService.execute {
                produceCardInfoInGameThread(gameEntry.game, playerId, cardId, responseWriter)
            }
        }
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
        game: Game<Any>,
        playerId: String,
        decisionId: String,
        decisionValue: String,
    ) {
        game.processDecision(playerId, decisionId, decisionValue)
    }

    private fun setPlayerObserveSettingsInThread(
        game: Game<Any>,
        playerId: String,
        settings: Any,
    ) {
        game.setPlayerObserveSettings(playerId, settings)
    }

    private fun joinGameInGameThread(
        game: Game<Any>,
        playerId: String,
        channelId: String,
        gameStream: GameStream<Any>,
    ) {
        game.joinGame(playerId, channelId, gameStream)
    }

    private fun leaveGameInGameThread(game: Game<Any>, channelId: String) {
        game.leaveGame(channelId)
    }

    private fun produceCardInfoInGameThread(game: Game<Any>, playerId: String, cardId: String, responseWriter: ResponseWriter) {
        responseWriter.writeHtmlResponse(game.produceCardInfo(playerId, cardId, responseWriter))
    }
}

internal class GameContainer(
    val executorService: ExecutorService,
    val games: MutableSet<String> = mutableSetOf(),
)

internal data class GameEntry(
    val game: Game<Any>,
    var communicatedStatus: String,
    var notifiedFinished: Boolean,
)
