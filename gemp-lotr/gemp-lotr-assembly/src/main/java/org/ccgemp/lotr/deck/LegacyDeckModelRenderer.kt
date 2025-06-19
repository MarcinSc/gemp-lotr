package org.ccgemp.lotr.deck

import com.gempukku.context.initializer.inject.Inject
import com.gempukku.context.resolver.expose.Exposes
import com.gempukku.lotro.game.BasicCardItem
import com.gempukku.lotro.game.CardItem
import com.gempukku.lotro.game.CardNotFoundException
import com.gempukku.lotro.game.LotroFormat
import com.gempukku.lotro.logic.vo.LotroDeck
import com.gempukku.server.ResponseWriter
import org.ccgemp.collection.FilterAndSort
import org.ccgemp.common.GameDeck
import org.ccgemp.deck.renderer.DeckModelRenderer
import org.ccgemp.lotr.LegacyObjectsProvider
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory

@Exposes(DeckModelRenderer::class)
class LegacyDeckModelRenderer : DeckModelRenderer {
    @Inject
    private lateinit var legacyObjectsProvider: LegacyObjectsProvider

    @Inject
    private lateinit var filterAndSort: FilterAndSort<Any>

    private val defaultSortComparator =
        Comparator.comparing { deck: GameDeck ->
            val format = legacyObjectsProvider.formatLibrary.getFormat(deck.targetFormat)
            String.format("%02d", format.order) + format.name + deck.name
        }

    override fun renderListDecks(player: String, playerDecks: List<GameDeck>, responseWriter: ResponseWriter) {
        responseWriter.writeXmlResponse(renderDeckList(playerDecks))
    }

    override fun renderGetDeck(player: String, deck: GameDeck, responseWriter: ResponseWriter) {
        responseWriter.writeXmlResponse(renderDeck(deck))
    }

    private fun renderDeck(deck: GameDeck): Document {
        return serializeDeck(deck.toLotroDeck())
    }

    fun renderDeckList(decks: List<GameDeck>, comparator: Comparator<GameDeck>? = null): Document {
        val sortedDecks = decks.sortedWith(comparator ?: defaultSortComparator)

        return renderFormatAndDeckNames(sortedDecks.map { it.name to legacyObjectsProvider.formatLibrary.getFormat(it.targetFormat) })
    }

    private fun renderFormatAndDeckNames(decks: List<Pair<String, LotroFormat>>): Document {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val doc = documentBuilder.newDocument()
        val decksElem = doc.createElement("decks")

        for ((deckName, deckFormat) in decks) {
            val deckElem = doc.createElement("deck")
            deckElem.textContent = deckName
            deckElem.setAttribute("targetFormat", deckFormat.name)
            decksElem.appendChild(deckElem)
        }
        doc.appendChild(decksElem)
        return doc
    }

    private fun serializeDeck(deck: LotroDeck): Document {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()

        val doc = documentBuilder.newDocument()
        val deckElem = doc.createElement("deck")
        doc.appendChild(deckElem)

        val validatedFormat: LotroFormat = validateFormat(deck.targetFormat)

        val targetFormat = doc.createElement("targetFormat")
        targetFormat.setAttribute("formatName", validatedFormat.name)
        targetFormat.setAttribute("formatCode", validatedFormat.code)
        deckElem.appendChild(targetFormat)

        val notes = doc.createElement("notes")
        notes.textContent = deck.notes
        deckElem.appendChild(notes)

        if (deck.ringBearer != null) {
            val ringBearer = doc.createElement("ringBearer")
            ringBearer.setAttribute("blueprintId", deck.ringBearer)
            deckElem.appendChild(ringBearer)
        }

        if (deck.ring != null) {
            val ring = doc.createElement("ring")
            ring.setAttribute("blueprintId", deck.ring)
            deckElem.appendChild(ring)
        }

        if (deck.map != null) {
            val map = doc.createElement("map")
            map.setAttribute("blueprintId", deck.map)
            deckElem.appendChild(map)
        }

        for (cardItem in filterAndSort.process<CardItem>("", "siteNumber,twilight", createCardItems(deck.sites))) {
            val site = doc.createElement("site")
            site.setAttribute("blueprintId", cardItem.blueprintId)
            deckElem.appendChild(site)
        }

        for (cardItem in filterAndSort.process<CardItem>("", "cardType,culture,name", createCardItems(deck.drawDeckCards))) {
            val card = doc.createElement("card")
            var side: String
            try {
                side = legacyObjectsProvider.cardLibrary.getLotroCardBlueprint(cardItem.blueprintId).getSide().toString()
            } catch (e: CardNotFoundException) {
                side = "FREE_PEOPLE"
            } catch (e: NullPointerException) {
                side = "FREE_PEOPLE"
            }
            card.setAttribute("side", side)
            card.setAttribute("blueprintId", cardItem.blueprintId)
            deckElem.appendChild(card)
        }

        return doc
    }

    private fun validateFormat(name: String): LotroFormat {
        val formatLibrary = legacyObjectsProvider.formatLibrary

        return formatLibrary.getFormat(name) ?: try {
            formatLibrary.getFormatByName(name)
        } catch (ex: Exception) {
            formatLibrary.getFormatByName("Anything Goes")
        }
    }

    private fun createCardItems(blueprintIds: List<String>): List<CardItem> {
        return blueprintIds.map { BasicCardItem(it) }
    }
}
