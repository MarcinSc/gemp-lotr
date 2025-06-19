package org.ccgemp.game

import com.gempukku.server.polling.GatheringStream

class GatheringGameStream<GameEvent> : GameStream<GameEvent> {
    val gatheringStream: GatheringStream<GameEvent> = GatheringStream()

    override fun processGameEvent(gameEvent: GameEvent) {
        gatheringStream.addEvent(gameEvent)
    }

    override fun gameClosed() {
        gatheringStream.setClosed()
    }
}
