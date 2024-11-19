package org.ccgemp.tournament

import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.game.GameContainerInterface
import org.ccgemp.game.GameParticipant
import org.ccgemp.game.GameSettings
import org.ccgemp.game.GameStream

@Exposes(GameContainerInterface::class)
class DummyGameContainer : GameContainerInterface<Any> {
    override fun createNewGame(participants: Array<GameParticipant>, gameSettings: GameSettings): String {
        TODO("Not yet implemented")
    }

    override fun joinGame(
        gameId: String,
        playerId: String,
        admin: Boolean,
        gameStream: GameStream<Any>,
    ): Runnable? {
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
}
