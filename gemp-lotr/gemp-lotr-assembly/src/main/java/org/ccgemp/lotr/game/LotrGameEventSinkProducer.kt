package org.ccgemp.lotr.game

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.state.EventSerializer
import com.gempukku.lotro.game.state.GameEvent
import com.gempukku.server.ResponseWriter
import com.gempukku.server.polling.EventSink
import org.ccgemp.game.GameEventSinkProducer
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(GameEventSinkProducer::class)
class LotrGameEventSinkProducer : GameEventSinkProducer<GameEvent> {
    private val eventSerializer: EventSerializer = EventSerializer()

    override fun createEventSink(gameId: String, pollId: String, responseWriter: ResponseWriter): EventSink<GameEvent> {
        return object : EventSink<GameEvent> {
            override fun processEventsAndClose(events: List<GameEvent>) {
                val documentBuilderFactory = DocumentBuilderFactory.newInstance()
                val documentBuilder = documentBuilderFactory.newDocumentBuilder()

                val doc = documentBuilder.newDocument()
                val update = doc.createElement("update")
                update.setAttribute("gameId", gameId)
                update.setAttribute("pollId", pollId)

                events.forEach {
                    eventSerializer.serializeEvent(doc, it)
                }

                doc.appendChild(update)

                responseWriter.writeXmlResponse(doc)
            }
        }
    }
}
