package org.ccgemp.game

import org.w3c.dom.Document
import org.w3c.dom.Element

interface GameEventSerializer<GameEvent> {
    fun serialize(player: String, document: Document, event: GameEvent): Element
}