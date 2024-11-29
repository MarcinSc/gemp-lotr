package org.ccgemp.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import org.ccgemp.common.GameDeck
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

        val deckPartMap = mutableMapOf<String, List<String>>()

        deckContents.forEach { section ->
            val deckPartName = section.name
            val deckPartContents =
                section.value.asArray().map {
                    it.asString()
                }
            deckPartMap.put(deckPartName, deckPartContents)
        }
        return GameDeck(name, notes, targetFormat, deckPartMap)
    }
}
