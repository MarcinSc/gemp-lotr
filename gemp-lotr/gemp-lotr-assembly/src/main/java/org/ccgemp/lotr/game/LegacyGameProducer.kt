package org.ccgemp.lotr.game

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.CardType
import com.gempukku.lotro.common.Keyword
import com.gempukku.lotro.common.Phase
import com.gempukku.lotro.common.Zone
import com.gempukku.lotro.filters.Filters
import com.gempukku.lotro.game.DefaultUserFeedback
import com.gempukku.lotro.game.LotroGameMediator
import com.gempukku.lotro.game.state.GameEvent
import com.gempukku.lotro.logic.GameUtils
import com.gempukku.lotro.logic.decisions.AwaitingDecision
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException
import com.gempukku.lotro.logic.modifiers.Modifier
import com.gempukku.lotro.logic.timing.DefaultLotroGame
import com.gempukku.server.ResponseWriter
import org.ccgemp.game.Game
import org.ccgemp.game.GameParticipant
import org.ccgemp.game.GameProducer
import org.ccgemp.game.GameResult
import org.ccgemp.game.GameSettings
import org.ccgemp.game.GameStream
import org.ccgemp.lotr.LegacyObjectsProvider
import org.ccgemp.lotr.deck.toLotroDeck
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

private val LOG: Logger = Logger.getLogger(LegacyGameProducer::class.simpleName)

@Exposes(GameProducer::class)
class LegacyGameProducer : GameProducer<Set<Phase>> {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    override fun createGame(gameId: String, gameParticipants: Array<GameParticipant>, gameSettings: GameSettings): Game<Set<Phase>> {
        val format = legacyObjectsProvider.formatLibrary.getFormat(gameSettings.format)
        val userFeedback = DefaultUserFeedback()
        val decks =
            gameParticipants.associateTo(mutableMapOf()) { player ->
                player.playerId to player.playerDeck.toLotroDeck()
            }
        val statusHolder = AtomicReference("Setting up")
        val game =
            DefaultLotroGame(
                format,
                decks,
                userFeedback,
                legacyObjectsProvider.cardLibrary,
                { status -> statusHolder.set(status) },
                gameSettings.timeSettings.toString(),
                !gameSettings.private,
            )
        return LegacyGame(gameId, gameParticipants, gameSettings, game, statusHolder, userFeedback)
    }
}

internal class LegacyGame(
    override val gameId: String,
    override val gameParticipants: Array<GameParticipant>,
    override val gameSettings: GameSettings,
    private val lotroGame: DefaultLotroGame,
    private val statusHolder: AtomicReference<String>,
    private val userFeedback: DefaultUserFeedback,
) : Game<Set<Phase>> {
    override val formatName: String
        get() = lotroGame.format.name
    override val info: String
        get() = gameSettings.info
    override val players: List<String>
        get() = gameParticipants.map { it.playerId }
    override val status: String
        get() = statusHolder.get()
    override val watchable: Boolean
        get() = gameSettings.watchable
    override val private: Boolean
        get() = gameSettings.private
    override val winner: String?
        get() = lotroGame.winnerPlayerId

    @Volatile
    override var gameResult: GameResult? = null

    private var playerClocks: MutableMap<String, Int> = HashMap()
    private val openChatStreams: MutableSet<GameStreamConfig> = mutableSetOf()

    init {
        gameParticipants.forEach {
            playerClocks[it.playerId] = 0
        }
    }

    override fun setPlayerObserveSettings(playerId: String, settings: Set<Phase>) {
        if (gameParticipants.any { it.playerId == playerId }) {
            lotroGame.setPlayerAutoPassSettings(playerId, settings)
        }
    }

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
        lotroGame.addGameResultListener(
            object : com.gempukku.lotro.logic.timing.GameResultListener {
                override fun gameCancelled() {
                    gameResult = GameResult(System.currentTimeMillis(), true, null)
                }

                override fun gameFinished(winnerPlayerId: String?, winReason: String?, loserPlayerIdsWithReasons: MutableMap<String, String>?) {
                    gameResult = GameResult(System.currentTimeMillis(), false, winnerPlayerId)
                }
            },
        )
        openChatStreams.add(GameStreamConfig(playerId, channelId, gameStream))
    }

    override fun produceCardInfo(playerId: String, cardId: String, responseWriter: ResponseWriter): String {
        val card = lotroGame.gameState.findCardById(cardId.toInt())
        if (card?.zone == null) return ""

        if (card.zone.isInPlay || card.zone == Zone.HAND) {
            val sb = StringBuilder()

            if (card.zone == Zone.HAND) {
                sb.append("<b>Card is in hand - stats are only provisional</b><br><br>")
            } else if (!Filters.hasActive(lotroGame, card)) {
                sb.append("<b>Card is inactive - current stats may be inaccurate</b><br><br>")
            }

            sb.append("<b>Affecting card:</b>")
            val modifiers: Collection<Modifier> = lotroGame.modifiersQuerying.getModifiersAffecting(lotroGame, card)
            for (modifier in modifiers) {
                val source = modifier.source
                if (source != null) {
                    sb.append("<br><b>")
                        .append(GameUtils.getCardLink(source))
                        .append(":</b> ")
                        .append(modifier.getText(lotroGame, card))
                } else {
                    sb.append("<br><b><i>System</i>:</b> ")
                        .append(modifier.getText(lotroGame, card))
                }
            }
            if (modifiers.isEmpty()) {
                sb.append("<br><i>nothing</i>")
            }

            if (card.zone.isInPlay && card.blueprint.cardType == CardType.SITE) {
                sb.append("<br><b>Owner:</b> ")
                    .append(card.owner)
            }

            val map = lotroGame.gameState.getTokens(card)
            if (map.isNotEmpty()) {
                sb.append("<br><b>Tokens:</b>")
                for ((key, value) in map) {
                    sb.append("<br>")
                        .append(key.toString())
                        .append(": ")
                        .append(value)
                }
            }

            val stackedCards = lotroGame.gameState.getStackedCards(card)
            if (stackedCards.isNotEmpty()) {
                sb.append("<br><b>Stacked cards:</b>")
                    .append("<br>")
                    .append(GameUtils.getAppendedNames(stackedCards))
            }

            val extraDisplayableInformation = card.blueprint.getDisplayableInformation(card)
            if (extraDisplayableInformation != null) {
                sb.append("<br><b>Extra information:</b>")
                    .append("<br>")
                    .append(extraDisplayableInformation)
            }

            sb.append("<br><br><b>Effective stats:</b>")
            try {
                val target = card.attachedTo
                val twilightCost = lotroGame.modifiersQuerying.getTwilightCostToPlay(lotroGame, card, target, 0, false)
                sb.append("<br><b>Twilight cost:</b> ")
                    .append(twilightCost)
            } catch (ignored: UnsupportedOperationException) {
            }
            try {
                val strength: Int = lotroGame.modifiersQuerying.getStrength(lotroGame, card)
                sb.append("<br><b>Strength:</b> ")
                    .append(strength)
            } catch (ignored: UnsupportedOperationException) {
            }
            try {
                val vitality: Int = lotroGame.modifiersQuerying.getVitality(lotroGame, card)
                sb.append("<br><b>Vitality:</b> ")
                    .append(vitality)
            } catch (ignored: UnsupportedOperationException) {
            }
            try {
                val resistance: Int = lotroGame.modifiersQuerying.getResistance(lotroGame, card)
                sb.append("<br><b>Resistance:</b> ")
                    .append(resistance)
            } catch (ignored: UnsupportedOperationException) {
            }
            try {
                val siteNumber: Int = lotroGame.modifiersQuerying.getMinionSiteNumber(lotroGame, card)
                sb.append("<br><b>Site number:</b> ")
                    .append(siteNumber)
            } catch (ignored: UnsupportedOperationException) {
            }

            val keywords = StringBuilder()
            for (keyword in Keyword.entries) {
                if (keyword.isInfoDisplayable) {
                    if (keyword.isMultiples) {
                        val count: Int = lotroGame.modifiersQuerying.getKeywordCount(lotroGame, card, keyword)
                        if (count > 0) {
                            keywords.append(keyword.humanReadable)
                                .append(" +")
                                .append(count)
                                .append(", ")
                        }
                    } else {
                        if (lotroGame.modifiersQuerying.hasKeyword(lotroGame, card, keyword)) {
                            keywords.append(keyword.humanReadable)
                                .append(", ")
                        }
                    }
                }
            }
            if (keywords.isNotEmpty()) {
                sb.append("<br><b>Keywords:</b> ")
                    .append(keywords.substring(0, keywords.length - 2))
            }

            if (LotroGameMediator.TrulyAwfulTengwarHackMap.containsKey(card.blueprintId)) {
                sb.append("<br><br><b>Tengwar Translation: </b><br>")
                    .append(LotroGameMediator.TrulyAwfulTengwarHackMap[card.blueprintId])
            }

            return sb.toString()
        } else {
            return ""
        }
    }

    override fun leaveGame(channelId: String) {
        openChatStreams.removeIf {
            it.channelId == channelId
        }
    }

    override fun cancelGame(playerId: String) {
        if (gameParticipants.any { it.playerId == playerId }) {
            lotroGame.requestCancel(playerId)
        }
    }

    override fun concedeGame(playerId: String) {
        if (gameParticipants.any { it.playerId == playerId }) {
            if (!lotroGame.isFinished) {
                lotroGame.playerLost(playerId, "Concession")
            }
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
