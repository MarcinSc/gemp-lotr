package com.gempukku.server.chat.polling.json

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.ChatEvent
import com.gempukku.server.chat.ChatEventSinkProducer
import com.gempukku.server.polling.EventSink

@Exposes(ChatEventSinkProducer::class)
class JsonChatEventSinkProducer : ChatEventSinkProducer {
    override fun createChatEventSink(room: String, pollId: String, responseWriter: ResponseWriter): EventSink<ChatEvent> =
        JsonEventSink(
            room,
            pollId,
            responseWriter,
        ) as EventSink<ChatEvent>
}
