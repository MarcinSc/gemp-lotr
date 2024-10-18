package com.gempukku.server.polling

interface LongPolling {
    fun <Event> registerLongPoll(eventStream: EventStream<Event>, timeoutRunnable: Runnable?): String

    fun <Event> registerSink(pollId: String, eventSink: EventSink<Event>): Boolean
}