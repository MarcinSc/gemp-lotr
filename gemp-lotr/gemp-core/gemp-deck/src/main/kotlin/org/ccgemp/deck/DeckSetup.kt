package org.ccgemp.deck

import org.ccgemp.deck.renderer.DeckModelRenderer
import org.ccgemp.deck.renderer.JsonDeckModelRenderer

fun createDeckSystems(
    deckModelRenderer: DeckModelRenderer = JsonDeckModelRenderer(),
    deckDeserializer: DeckDeserializer = JsonDeckDeserializer(),
    dbDeckSerialization: DbDeckSerialization = SimpleDbDeckSerialization(),
): List<Any> {
    return listOf(
        DeckSystem(),
        DbDeckRepository(),
        DeckApiSystem(),
        deckModelRenderer,
        deckDeserializer,
        dbDeckSerialization,
    )
}
