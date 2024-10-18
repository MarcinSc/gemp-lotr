package com.gempukku.server.polling


class GatheringStream<Event> : EventStream<Event> {
    private val pendingEvents: MutableList<Event> = mutableListOf()
    private var closed: Boolean = false

    fun addEvent(event: Event) {
        pendingEvents.add(event)
    }

    fun setClosed() {
        closed = true
    }

    override fun consumeEvents(): List<Event> {
        val result = ArrayList<Event>(pendingEvents)
        pendingEvents.clear()
        return result
    }

    override fun isFinished(): Boolean {
        return closed && pendingEvents.isEmpty()
    }
}