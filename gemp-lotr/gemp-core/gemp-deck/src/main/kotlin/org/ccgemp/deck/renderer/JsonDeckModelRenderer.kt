package org.ccgemp.deck.renderer

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.server.ResponseWriter
import org.ccgemp.common.GameDeck
import org.hjson.JsonArray
import org.hjson.JsonObject

@Exposes(DeckModelRenderer::class)
class JsonDeckModelRenderer : DeckModelRenderer {
    override fun renderGetDeck(player: String, deck: GameDeck, responseWriter: ResponseWriter) {
        val root = JsonObject()
        root.set("player", player)
        root.set("name", deck.name)
        root.set("notes", deck.notes)
        root.set("targetFormat", deck.targetFormat)
        val parts = JsonArray()
        deck.deckParts.forEach { part ->
            val jsonPart = JsonObject()
            jsonPart.set("name", part.key)
            val cards = JsonObject()
            part.value.forEach { card ->
                cards.set(card.card, card.count)
            }
            jsonPart.set("cards", cards)
            parts.add(jsonPart)
        }
        root.set("contents", parts)

        responseWriter.writeJsonResponse(root.toString())
    }

    override fun renderListDecks(player: String, playerDecks: List<GameDeck>, responseWriter: ResponseWriter) {
        val root = JsonObject()
        root.set("player", player)
        val decks = JsonArray()
        playerDecks.forEach { playerDeck ->
            val deck = JsonObject()
            deck.set("name", playerDeck.name)
            deck.set("notes", playerDeck.notes)
            deck.set("targetFormat", playerDeck.targetFormat)
            decks.add(deck)
        }
        root.set("decks", decks)
        responseWriter.writeJsonResponse(root.toString())
    }
}
