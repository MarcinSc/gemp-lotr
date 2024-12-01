package org.ccgemp.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.GameDeck
import org.ccgemp.common.GameDeckItem
import org.ccgemp.json.JsonProvider

@Exposes(DeckDeserializer::class)
class JsonDeckDeserializer : DeckDeserializer {
    @Inject
    private lateinit var jsonProvider: JsonProvider

    override fun deserializeDeck(
        name: String,
        targetFormat: String,
        notes: String,
        contents: String,
    ): GameDeck {
        val deckContents = jsonProvider.readJson(contents)

        val deckPartMap = mutableMapOf<String, List<GameDeckItem>>()

        deckContents.forEach { section ->
            val deckPartName = section.name
            val deckPartContents =
                section.value.asArray().map {
                    it.asString()
                }.groupingBy { it }.eachCount()
            deckPartMap.put(deckPartName, deckPartContents.map { cardCount -> GameDeckItem(cardCount.key, cardCount.value) })
        }
        return GameDeck(name, notes, targetFormat, deckPartMap)
    }
}
