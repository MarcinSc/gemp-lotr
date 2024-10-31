package com.gempukku.server.polling

interface EventSink<Event> {
    fun processEventsAndClose(events: List<Event>)
}
