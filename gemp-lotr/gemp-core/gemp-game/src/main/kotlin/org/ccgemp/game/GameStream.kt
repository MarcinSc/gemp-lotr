package org.ccgemp.game

interface GameStream<Event> {
    fun processGameEvent(gameEvent: Event)
    fun gameClosed()
}
