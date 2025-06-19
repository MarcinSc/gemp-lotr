package org.ccgemp.tournament

import com.gempukku.context.Registration
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.game.GameParticipant
import org.ccgemp.game.GameSettings
import org.ccgemp.game.GameStream

@Exposes(GameContainerInterface::class)
class DummyGameContainer<GameEvent, ObserveSettings> : GameContainerInterface<GameEvent, ObserveSettings> {
    override fun createNewGame(participants: Array<GameParticipant>, gameSettings: GameSettings): String {
        TODO("Not yet implemented")
    }

    override fun joinGame(
        gameId: String,
        playerId: String,
        admin: Boolean,
        gameStream: GameStream<GameEvent>,
    ): Registration? {
        TODO("Not yet implemented")
    }

    override fun setPlayerObserveSettings(gameId: String, player: String, settings: ObserveSettings) {
        TODO("Not yet implemented")
    }

    override fun processPlayerDecision(
        gameId: String,
        playerId: String,
        decisionId: String,
        decisionValue: String,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun produceCardInfo(
        gameId: String,
        playerId: String,
        cardId: String,
        responseWriter: ResponseWriter,
    ) {
        responseWriter.writeHtmlResponse("")
    }

    override fun cancel(gameId: String, playerId: String) {
    }

    override fun concede(gameId: String, playerId: String) {
    }
}
