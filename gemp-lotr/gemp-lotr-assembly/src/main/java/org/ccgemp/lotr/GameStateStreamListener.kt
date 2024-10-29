package org.ccgemp.lotr

import com.gempukku.lotro.common.Token
import com.gempukku.lotro.common.Zone
import com.gempukku.lotro.communication.GameStateListener
import com.gempukku.lotro.game.LotroFormat
import com.gempukku.lotro.game.PhysicalCard
import com.gempukku.lotro.game.state.GameEvent
import com.gempukku.lotro.game.state.PreGameInfo
import com.gempukku.lotro.logic.decisions.AwaitingDecision
import com.gempukku.lotro.logic.timing.GameStats
import org.ccgemp.game.GameStream
import java.util.LinkedList

class GameStateStreamListener(
    private val playerId: String,
    private val format: LotroFormat,
    private val stream: GameStream<GameEvent>,
) : GameStateListener {
    private fun appendEvent(event: GameEvent) {
        stream.processGameEvent(event)
    }

    override fun initializeBoard(
        participants: List<String>?,
        discardIsPublic: Boolean,
    ) {
        val participantIds: MutableList<String> = LinkedList()
        participantIds.addAll(participants!!)
        appendEvent(
            GameEvent(GameEvent.Type.PARTICIPANTS)
                .participantId(playerId)
                .allParticipantIds(participantIds)
                .discardPublic(discardIsPublic),
        )
    }

    override fun initializePregameBoard(preGameInfo: PreGameInfo) {
        val participantIds: List<String> = LinkedList(preGameInfo.participants)
        appendEvent(
            GameEvent(GameEvent.Type.PRE_GAME_SETUP)
                .participantId(playerId)
                .allParticipantIds(participantIds)
                .preGameInfo(preGameInfo),
        )
    }

    private fun getCardIds(cards: Collection<PhysicalCard>): IntArray = cards.map { it.cardId }.toIntArray()

    override fun addAssignment(
        freePeople: PhysicalCard,
        minions: Set<PhysicalCard>,
    ) {
        appendEvent(
            GameEvent(GameEvent.Type.ADD_ASSIGNMENT).cardId(freePeople.cardId).otherCardIds(getCardIds(minions)),
        )
    }

    override fun removeAssignment(freePeople: PhysicalCard) {
        appendEvent(GameEvent(GameEvent.Type.REMOVE_ASSIGNMENT).cardId(freePeople.cardId))
    }

    override fun startSkirmish(
        freePeople: PhysicalCard?,
        minions: Set<PhysicalCard>,
    ) {
        val gameEvent = GameEvent(GameEvent.Type.START_SKIRMISH).otherCardIds(getCardIds(minions))
        if (freePeople != null) gameEvent.cardId(freePeople.cardId)
        appendEvent(gameEvent)
    }

    override fun addToSkirmish(card: PhysicalCard?) {
        appendEvent(GameEvent(GameEvent.Type.ADD_TO_SKIRMISH).card(card))
    }

    override fun removeFromSkirmish(card: PhysicalCard?) {
        appendEvent(GameEvent(GameEvent.Type.REMOVE_FROM_SKIRMISH).card(card))
    }

    override fun finishSkirmish() {
        appendEvent(GameEvent(GameEvent.Type.END_SKIRMISH))
    }

    override fun setCurrentPhase(phase: String?) {
        appendEvent(GameEvent(GameEvent.Type.GAME_PHASE_CHANGE).phase(phase))
    }

    override fun cardCreated(card: PhysicalCard) {
        val publicDiscard = card.zone == Zone.DISCARD && format.discardPileIsPublic()
        if (card.zone.isPublic || publicDiscard || (card.zone.isVisibleByOwner && card.owner == playerId)) {
            appendEvent(
                GameEvent(GameEvent.Type.PUT_CARD_INTO_PLAY).card(card),
            )
        }
    }

    override fun cardCreated(
        card: PhysicalCard,
        overridePlayerVisibility: Boolean,
    ) {
        val publicDiscard = card.zone == Zone.DISCARD && format.discardPileIsPublic()
        if (card.zone.isPublic ||
            publicDiscard ||
            ((overridePlayerVisibility || card.zone.isVisibleByOwner) && (card.owner == playerId))
        ) {
            appendEvent(
                GameEvent(GameEvent.Type.PUT_CARD_INTO_PLAY).card(card),
            )
        }
    }

    override fun cardMoved(card: PhysicalCard?) {
        appendEvent(GameEvent(GameEvent.Type.MOVE_CARD_IN_PLAY).card(card))
    }

    override fun cardsRemoved(
        playerPerforming: String?,
        cards: Collection<PhysicalCard>,
    ) {
        val removedCardsVisibleByPlayer: MutableSet<PhysicalCard> = HashSet()
        for (card in cards) {
            val publicDiscard = card.zone == Zone.DISCARD && format.discardPileIsPublic()
            if (card.zone.isPublic ||
                publicDiscard ||
                (card.zone.isVisibleByOwner && card.owner == playerId)
            ) {
                removedCardsVisibleByPlayer.add(
                    card,
                )
            }
        }
        if (removedCardsVisibleByPlayer.size > 0) {
            appendEvent(
                GameEvent(GameEvent.Type.REMOVE_CARD_FROM_PLAY)
                    .otherCardIds(
                        getCardIds(removedCardsVisibleByPlayer),
                    ).participantId(playerPerforming),
            )
        }
    }

    override fun setPlayerPosition(
        participant: String?,
        position: Int,
    ) {
        appendEvent(GameEvent(GameEvent.Type.PLAYER_POSITION).participantId(participant).index(position))
    }

    override fun setTwilight(twilightPool: Int) {
        appendEvent(GameEvent(GameEvent.Type.TWILIGHT_POOL_UPDATE).count(twilightPool))
    }

    override fun setCurrentPlayerId(currentPlayerId: String?) {
        appendEvent(GameEvent(GameEvent.Type.TURN_CHANGE).participantId(currentPlayerId))
    }

    override fun getAssignedPlayerId(): String = playerId

    override fun addTokens(
        card: PhysicalCard?,
        token: Token?,
        count: Int,
    ) {
        appendEvent(GameEvent(GameEvent.Type.ADD_TOKENS).card(card).token(token).count(count))
    }

    override fun removeTokens(
        card: PhysicalCard?,
        token: Token?,
        count: Int,
    ) {
        appendEvent(GameEvent(GameEvent.Type.REMOVE_TOKENS).card(card).token(token).count(count))
    }

    override fun sendMessage(message: String?) {
        appendEvent(GameEvent(GameEvent.Type.SEND_MESSAGE).message(message))
    }

    override fun setSite(card: PhysicalCard) {
        appendEvent(GameEvent(GameEvent.Type.PUT_CARD_INTO_PLAY).card(card).index(card.siteNumber))
    }

    override fun sendGameStats(gameStats: GameStats) {
        appendEvent(GameEvent(GameEvent.Type.GAME_STATS).gameStats(gameStats.makeACopy()))
    }

    override fun cardAffectedByCard(
        playerPerforming: String?,
        card: PhysicalCard?,
        affectedCards: Collection<PhysicalCard>,
    ) {
        appendEvent(
            GameEvent(GameEvent.Type.CARD_AFFECTED_BY_CARD)
                .card(card)
                .participantId(playerPerforming)
                .otherCardIds(getCardIds(affectedCards)),
        )
    }

    override fun eventPlayed(card: PhysicalCard?) {
        appendEvent(GameEvent(GameEvent.Type.SHOW_CARD_ON_SCREEN).card(card))
    }

    override fun cardActivated(
        playerPerforming: String?,
        card: PhysicalCard?,
    ) {
        appendEvent(GameEvent(GameEvent.Type.FLASH_CARD_IN_PLAY).card(card).participantId(playerPerforming))
    }

    override fun decisionRequired(
        playerId: String,
        decision: AwaitingDecision?,
    ) {
        if (playerId == this.playerId) {
            appendEvent(
                GameEvent(GameEvent.Type.DECISION).awaitingDecision(decision).participantId(playerId),
            )
        }
    }

    override fun sendWarning(
        playerId: String,
        warning: String?,
    ) {
        if (playerId == this.playerId) appendEvent(GameEvent(GameEvent.Type.SEND_WARNING).message(warning))
    }

    override fun endGame() {
        appendEvent(GameEvent(GameEvent.Type.GAME_ENDED))
    }
}
