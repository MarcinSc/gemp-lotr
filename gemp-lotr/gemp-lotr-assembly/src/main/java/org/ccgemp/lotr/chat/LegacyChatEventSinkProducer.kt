package org.ccgemp.lotr.chat

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import com.gempukku.server.chat.ChatEvent
import com.gempukku.server.chat.ChatEventSinkProducer
import com.gempukku.server.chat.ChatInterface
import com.gempukku.server.polling.EventSink

@Exposes(ChatEventSinkProducer::class)
class LegacyChatEventSinkProducer : ChatEventSinkProducer {
    @Inject
    private lateinit var chatInterface: ChatInterface

    @Inject(allowsNull = true)
    private var chatNameDisplayFormatter: ChatNameDisplayFormatter? = null

    override fun createChatEventSink(room: String, pollId: String, responseWriter: ResponseWriter): EventSink<ChatEvent> =
        LegacyEventSink(
            room,
            pollId,
            chatInterface,
            chatNameDisplayFormatter,
            responseWriter,
        ) as EventSink<ChatEvent>
}