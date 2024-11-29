package org.ccgemp.lotr.deck

import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.logic.vo.LotroDeck
import org.ccgemp.deck.DbDeckSerialization
import org.ccgemp.common.GameDeck

@Exposes(DbDeckSerialization::class)
class LotrDbDeckSerialization : DbDeckSerialization {
    override fun serializeDeck(deck: GameDeck): String {
        val lotroDeck = deck.toLotroDeck()
        return com.gempukku.lotro.db.DeckSerialization.buildContentsFromDeck(lotroDeck)
    }

    override fun deserializeDeck(
        name: String,
        notes: String,
        targetFormat: String,
        deck: String,
    ): GameDeck {
        val lotroDeck = com.gempukku.lotro.db.DeckSerialization.buildDeckFromContents(name, deck, targetFormat, notes)
        return lotroDeck.toGameDeck()
    }

    fun buildContentsFromDeck(deck: LotroDeck): String {
        val sb = StringBuilder()
        if (deck.ringBearer != null) sb.append(deck.ringBearer)
        sb.append("|")
        if (deck.ring != null) sb.append(deck.ring)
        sb.append("|")
        for (i in deck.sites.indices) {
            if (i > 0) sb.append(",")
            sb.append(deck.sites[i])
        }
        sb.append("|")
        for (i in deck.drawDeckCards.indices) {
            if (i > 0) sb.append(",")
            sb.append(deck.drawDeckCards[i])
        }
        if (deck.map != null) {
            sb.append("|")
            sb.append(deck.map)
        }

        return sb.toString()
    }

    fun buildDeckFromContents(
        deckName: String?,
        contents: String,
        targetFormat: String?,
        notes: String?,
    ): LotroDeck {
        // New format
        val parts = contents.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val deck = LotroDeck(deckName)
        deck.targetFormat = targetFormat
        deck.notes = notes
        if (parts.size > 0 && parts[0] != "") deck.ringBearer = parts[0]
        if (parts.size > 1 && parts[1] != "") deck.ring = parts[1]
        if (parts.size > 2) {
            for (site in parts[2].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (site != "") deck.addSite(site)
            }
        }
        if (parts.size > 3) {
            for (card in parts[3].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (card != "") deck.addCard(card)
            }
        }
        if (parts.size > 4) {
            for (card in parts[4].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                if (card != "") deck.map = card
            }
        }

        return deck
    }
}
