package org.ccgemp.game

import com.gempukku.server.ResponseWriter
import com.gempukku.server.polling.EventSink

interface GameEventSinkProducer<GameEvent> {
    fun createEventSink(gameId: String, pollId: String, responseWriter: ResponseWriter): EventSink<GameEvent>
}
