package org.ccgemp.lotr.deck

import org.ccgemp.common.GameDeck

interface HtmlDeckSerializer {
    fun serializeDeck(author: String?, deck: GameDeck): String

    fun serializeValidation(deck: GameDeck, targetFormat: String): String
}
