package org.ccgemp.lotr.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.common.CardType
import com.gempukku.lotro.db.DeckSerialization
import com.gempukku.lotro.game.CardNotFoundException
import com.gempukku.lotro.game.LotroCardBlueprint
import com.gempukku.lotro.logic.vo.LotroDeck
import org.ccgemp.common.GameDeck
import org.ccgemp.deck.DeckDeserializer
import org.ccgemp.lotr.LegacyObjectsProvider
import java.util.Arrays

@Exposes(DeckDeserializer::class)
class LotrDeckDeserializer : DeckDeserializer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    override fun deserializeDeck(
        name: String,
        targetFormat: String,
        notes: String,
        contents: String,
    ): GameDeck? {
        if (contents.contains("|")) {
            // New format
            var cnt = 0
            for (c in contents.toCharArray()) {
                if (c == '|') cnt++
            }

            if (cnt < 3 || cnt > 4) return null

            return buildDeckFromContents(name, contents, targetFormat, notes)
        } else {
            // Old format
            val cards = listOf(*contents.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            if (cards.size < 2) return null

            return buildDeckFromContents(name, contents, targetFormat, notes)
        }
    }

    private fun buildDeckFromContents(
        name: String,
        targetFormat: String,
        notes: String,
        contents: String,
    ): GameDeck {
        if (contents.contains("|")) {
            return DeckSerialization.buildDeckFromContents(name, contents, targetFormat, notes).toGameDeck()
        } else {
            // Old format
            val cardsList = Arrays.asList(*contents.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            val ringBearer = cardsList[0]
            val ring = cardsList[1]
            val lotroDeck = LotroDeck(name)
            lotroDeck.targetFormat = targetFormat
            lotroDeck.notes = notes
            if (ringBearer.length > 0) lotroDeck.ringBearer = ringBearer
            if (ring.length > 0) lotroDeck.ring = ring
            for (blueprintId in cardsList.subList(2, cardsList.size)) {
                val cardBlueprint: LotroCardBlueprint
                try {
                    cardBlueprint = legacyObjectsProvider.cardLibrary.getLotroCardBlueprint(blueprintId)
                    if (cardBlueprint.cardType == CardType.SITE) {
                        lotroDeck.addSite(blueprintId)
                    } else {
                        lotroDeck.addCard(blueprintId)
                    }
                } catch (e: CardNotFoundException) {
                    // Ignore the card
                }
            }

            return lotroDeck.toGameDeck()
        }
    }
}
