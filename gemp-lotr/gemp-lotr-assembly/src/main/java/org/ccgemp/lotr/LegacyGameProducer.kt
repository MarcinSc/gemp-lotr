package org.ccgemp.lotr

import com.gempukku.context.processor.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.DefaultUserFeedback
import com.gempukku.lotro.game.state.GameEvent
import com.gempukku.lotro.logic.decisions.AwaitingDecision
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException
import com.gempukku.lotro.logic.timing.DefaultLotroGame
import org.ccgemp.game.Game
import org.ccgemp.game.GameParticipant
import org.ccgemp.game.GameProducer
import org.ccgemp.game.GameSettings
import org.ccgemp.game.GameStream
import java.util.logging.Level
import java.util.logging.Logger

private val LOG: Logger = Logger.getLogger(LegacyGameProducer::class.simpleName)

@Exposes(GameProducer::class)
class LegacyGameProducer : GameProducer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    override fun createGame(gameParticipants: Array<GameParticipant>, gameSettings: GameSettings): Game {
        val format = legacyObjectsProvider.formatLibrary.getFormat(gameSettings.format)
        val userFeedback = DefaultUserFeedback()
        val decks =
            gameParticipants.associateTo(mutableMapOf()) { player ->
                player.playerId to player.playerDeck.toLotroDeck()
            }
        val game =
            DefaultLotroGame(
                format,
                decks,
                userFeedback,
                legacyObjectsProvider.cardLibrary,
                gameSettings.timeSettings.toString(),
                !gameSettings.private,
                null,
            )
        return LegacyGame(gameParticipants, gameSettings, game, userFeedback)
    }
}

internal class LegacyGame(
    override val gameParticipants: Array<GameParticipant>,
    override val gameSettings: GameSettings,
    private val lotroGame: DefaultLotroGame,
    private val userFeedback: DefaultUserFeedback,
) : Game {
    private var gameFinishedTime: Long? = null
    private var playerClocks: MutableMap<String, Int> = HashMap()
    private val openChatStreams: MutableSet<GameStreamConfig> = mutableSetOf()

    init {
        gameParticipants.forEach {
            playerClocks[it.playerId] = 0
        }
    }

    override val gameFinished: Long?
        get() = gameFinishedTime

    override fun processDecision(playerId: String, decisionId: String, decisionValue: String) {
        val awaitingDecision = userFeedback.getAwaitingDecision(playerId)
        if (awaitingDecision != null) {
            if (awaitingDecision.awaitingDecisionId.toString() == decisionId && !lotroGame.isFinished) {
                try {
                    userFeedback.participantDecided(playerId)
                    awaitingDecision.decisionMade(decisionValue)

                    // Decision successfully made, add the time to user clock
                    addTimeSpentOnDecisionToUserClock(playerId, awaitingDecision)

                    lotroGame.carryOutPendingActionsUntilDecisionNeeded()
                } catch (decisionResultInvalidException: DecisionResultInvalidException) {
                    // Participant provided wrong answer - send a warning message, and ask again for the same decision
                    lotroGame.gameState.sendWarning(playerId, decisionResultInvalidException.warningMessage)
                    userFeedback.sendAwaitingDecision(playerId, awaitingDecision)
                } catch (runtimeException: RuntimeException) {
                    LOG.log(Level.SEVERE, "Error processing game decision", runtimeException)
                    lotroGame.cancelGame()
                }
            }
        }
    }

    override fun checkForTimeouts() {
        val currentTime = System.currentTimeMillis()
        if (!lotroGame.isFinished && !lotroGame.isCancelled) {
            userFeedback.usersPendingDecision.forEach { playerId ->
                val awaitingDecision = userFeedback.getAwaitingDecision(playerId)
                awaitingDecision?.let { decision ->
                    if (currentTime > decision.creationTime + gameSettings.timeSettings.maxSecondsPerDecision * 1000L) {
                        lotroGame.playerLost(playerId, "Player decision timed-out")
                    }
                }
            }
        }
        if (!lotroGame.isFinished && !lotroGame.isCancelled) {
            playerClocks.forEach { (playerId, usedTimeSeconds) ->
                val awaitingDecision = userFeedback.getAwaitingDecision(playerId)
                val currentDecisionSeconds =
                    awaitingDecision?.let {
                        (currentTime - awaitingDecision.creationTime) / 1000
                    } ?: 0
                if (gameSettings.timeSettings.maxSecondsPerPlayer > usedTimeSeconds + currentDecisionSeconds) {
                    lotroGame.playerLost(playerId, "Player run out of time")
                }
            }
        }
    }

    private fun addTimeSpentOnDecisionToUserClock(participantId: String, decision: AwaitingDecision) {
        val queryTime: Long = decision.creationTime
        val currentTime = System.currentTimeMillis()
        val diffSec = ((currentTime - queryTime) / 1000).toInt()
        if (diffSec > gameSettings.decisionLeniencySeconds) {
            playerClocks.compute(participantId) { _: String, value: Int? ->
                value!! + diffSec
            }
        }
    }

    override fun joinGame(playerId: String, channelId: String, gameStream: GameStream<Any>) {
        lotroGame.addGameStateListener(
            playerId,
            GameStateStreamListener(playerId, lotroGame.format, gameStream as GameStream<GameEvent>),
        )
        openChatStreams.add(GameStreamConfig(playerId, channelId, gameStream))
    }

    override fun leaveGame(channelId: String) {
        openChatStreams.removeIf {
            it.channelId == channelId
        }
    }

    override fun finalizeGame() {
        openChatStreams.forEach {
            it.gameStream.gameClosed()
        }
        openChatStreams.clear()
    }
}

private data class GameStreamConfig(
    val playerId: String,
    val channelId: String,
    val gameStream: GameStream<GameEvent>,
)
