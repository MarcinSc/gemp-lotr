package com.gempukku.server.polling

interface EventStream<Event> {
    fun consumeEvents(): List<Event>

    fun isFinished(): Boolean
}