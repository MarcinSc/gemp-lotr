package org.ccgemp.deck

import org.ccgemp.common.GameDeck

interface HtmlDeckSerializer {
    fun serializeDeck(deck: GameDeck, author: String?): String
    fun serializeValidation(deck: GameDeck, targetFormat: String): String
}