package com.gempukku.server.chat

import com.gempukku.server.ResponseWriter
import com.gempukku.server.polling.EventSink

interface ChatEventSinkProducer {
    fun createChatEventSink(room: String, pollId: String, responseWriter: ResponseWriter): EventSink<ChatEvent>
}
