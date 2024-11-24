package org.ccgemp.game

import com.gempukku.context.Registration
import com.gempukku.server.ResponseWriter

interface GameContainerInterface<Event, ObserveSettings> {
    fun createNewGame(participants: Array<GameParticipant>, gameSettings: GameSettings): String

    fun setPlayerObserveSettings(gameId: String, player: String, settings: ObserveSettings)

    fun joinGame(
        gameId: String,
        playerId: String,
        admin: Boolean,
        gameStream: GameStream<Event>,
    ): Registration?

    fun processPlayerDecision(
        gameId: String,
        playerId: String,
        decisionId: String,
        decisionValue: String,
    ): Boolean

    fun produceCardInfo(
        gameId: String,
        playerId: String,
        cardId: String,
        responseWriter: ResponseWriter,
    )
}
