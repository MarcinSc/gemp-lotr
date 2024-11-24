package com.gempukku.server.polling

import com.gempukku.context.Registration

interface LongPolling {
    fun <Event> registerLongPoll(eventStream: EventStream<Event>, registration: Registration?): String

    fun <Event> registerSink(pollId: String, eventSink: EventSink<Event>): Boolean
}
